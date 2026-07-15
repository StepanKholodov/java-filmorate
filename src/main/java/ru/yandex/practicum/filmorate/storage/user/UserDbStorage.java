package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация {@link UserStorage}, хранящая пользователей в реляционной базе данных
 * через {@link JdbcTemplate}. Список друзей хранится в таблице {@code friendship}
 * (односторонняя связь {@code user_id -> friend_id}) и подгружается отдельным
 * запросом при чтении.
 */
@Slf4j
@Repository("userDbStorage")
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private static final String INSERT_USER =
            "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
    private static final String DELETE_USER = "DELETE FROM users WHERE user_id = ?";
    private static final String FIND_ALL_USERS = "SELECT * FROM users";
    private static final String FIND_USER_BY_ID = "SELECT * FROM users WHERE user_id = ?";
    private static final String INSERT_FRIEND =
            "MERGE INTO friendship (user_id, friend_id) KEY (user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND =
            "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String DELETE_FRIENDSHIP_BY_USER =
            "DELETE FROM friendship WHERE user_id = ? OR friend_id = ?";
    private static final String DELETE_LIKES_BY_USER = "DELETE FROM likes WHERE user_id = ?";

    private final JdbcTemplate jdbcTemplate;

    /**
     * {@inheritDoc}
     */
    @Override
    public User add(User user) {
        useLoginAsNameIfBlank(user);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_USER, new String[]{"user_id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        user.setId(keyHolder.getKey().longValue());
        log.info("Пользователь добавлен с id = {}", user.getId());
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Long id) {
        ensureExists(id);
        jdbcTemplate.update(DELETE_FRIENDSHIP_BY_USER, id, id);
        jdbcTemplate.update(DELETE_LIKES_BY_USER, id);
        jdbcTemplate.update(DELETE_USER, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User modify(User user) {
        if (user.getId() == null) {
            log.warn("Ошибка обновления пользователя: id не указан");
            throw new ValidateException("id должен быть указан");
        }
        ensureExists(user.getId());
        useLoginAsNameIfBlank(user);
        jdbcTemplate.update(UPDATE_USER, user.getEmail(), user.getLogin(), user.getName(),
                Date.valueOf(user.getBirthday()), user.getId());
        log.info("Пользователь с id = {} обновлён", user.getId());
        return findById(user.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<User> getAll() {
        List<User> users = jdbcTemplate.query(FIND_ALL_USERS, new UserRowMapper());
        attachFriends(users);
        return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User findById(Long id) {
        User user = queryUserById(id);
        attachFriends(List.of(user));
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> findAllByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT * FROM users WHERE user_id IN (" + placeholders + ")";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), ids.toArray());
        attachFriends(users);
        return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFriend(Long userId, Long friendId) {
        ensureExists(userId);
        ensureExists(friendId);
        jdbcTemplate.update(INSERT_FRIEND, userId, friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFriend(Long userId, Long friendId) {
        ensureExists(userId);
        ensureExists(friendId);
        jdbcTemplate.update(DELETE_FRIEND, userId, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    /**
     * Читает пользователя по идентификатору без списка друзей.
     *
     * @param id идентификатор пользователя
     * @return пользователь с заполненными собственными полями
     * @throws NotFoundException если пользователь с таким идентификатором не найден
     */
    private User queryUserById(Long id) {
        try {
            return jdbcTemplate.queryForObject(FIND_USER_BY_ID, new UserRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Пользователь с id = {} не найден", id);
            throw new NotFoundException("пользователь с id = " + id + " не найден");
        }
    }

    /**
     * Проверяет, что пользователь с указанным идентификатором существует.
     *
     * @param id идентификатор пользователя
     * @throws NotFoundException если пользователь с таким идентификатором не найден
     */
    private void ensureExists(Long id) {
        queryUserById(id);
    }

    /**
     * Догружает список друзей для всех переданных пользователей одним запросом
     * вместо отдельного запроса на каждого пользователя.
     */
    private void attachFriends(List<User> users) {
        if (users.isEmpty()) {
            return;
        }
        List<Long> userIds = users.stream().map(User::getId).toList();
        String placeholders = String.join(",", Collections.nCopies(userIds.size(), "?"));
        String sql = "SELECT user_id, friend_id FROM friendship WHERE user_id IN (" + placeholders + ")";

        Map<Long, List<Long>> friendsByUser = new LinkedHashMap<>();
        jdbcTemplate.query(sql, (RowCallbackHandler) rs ->
                friendsByUser.computeIfAbsent(rs.getLong("user_id"), id -> new ArrayList<>())
                        .add(rs.getLong("friend_id")),
                userIds.toArray());

        for (User user : users) {
            user.getFriends().addAll(friendsByUser.getOrDefault(user.getId(), List.of()));
        }
    }

    /**
     * Если у пользователя не задано имя для отображения,
     * подставляет в него логин.
     *
     * @param user пользователь, у которого может быть пустое имя
     */
    private void useLoginAsNameIfBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пустое, используется логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}
