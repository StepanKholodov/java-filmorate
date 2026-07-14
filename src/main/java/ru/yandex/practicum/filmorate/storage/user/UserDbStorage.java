package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;

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
    private static final String FIND_FRIEND_IDS = "SELECT friend_id FROM friendship WHERE user_id = ?";
    private static final String INSERT_FRIEND =
            "MERGE INTO friendship (user_id, friend_id) KEY (user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND =
            "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String DELETE_FRIENDSHIP_BY_USER =
            "DELETE FROM friendship WHERE user_id = ? OR friend_id = ?";
    private static final String DELETE_LIKES_BY_USER = "DELETE FROM likes WHERE user_id = ?";

    private final JdbcTemplate jdbcTemplate;

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

    @Override
    public void remove(Long id) {
        ensureExists(id);
        jdbcTemplate.update(DELETE_FRIENDSHIP_BY_USER, id, id);
        jdbcTemplate.update(DELETE_LIKES_BY_USER, id);
        jdbcTemplate.update(DELETE_USER, id);
    }

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

    @Override
    public Collection<User> getAll() {
        List<User> users = jdbcTemplate.query(FIND_ALL_USERS, new UserRowMapper());
        users.forEach(this::loadFriends);
        return users;
    }

    @Override
    public User findById(Long id) {
        User user = queryUserById(id);
        loadFriends(user);
        return user;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        ensureExists(userId);
        ensureExists(friendId);
        jdbcTemplate.update(INSERT_FRIEND, userId, friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        ensureExists(userId);
        ensureExists(friendId);
        jdbcTemplate.update(DELETE_FRIEND, userId, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    private User queryUserById(Long id) {
        try {
            return jdbcTemplate.queryForObject(FIND_USER_BY_ID, new UserRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Пользователь с id = {} не найден", id);
            throw new NotFoundException("пользователь с id = " + id + " не найден");
        }
    }

    private void ensureExists(Long id) {
        queryUserById(id);
    }

    private void loadFriends(User user) {
        user.getFriends().addAll(jdbcTemplate.queryForList(FIND_FRIEND_IDS, Long.class, user.getId()));
    }

    private void useLoginAsNameIfBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пустое, используется логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}
