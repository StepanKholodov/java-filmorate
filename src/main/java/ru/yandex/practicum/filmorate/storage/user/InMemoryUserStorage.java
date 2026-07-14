package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Реализация {@link UserStorage}, хранящая пользователей в оперативной памяти приложения.
 * Используется как Spring-бин с уникальным жизненным циклом приложения (singleton).
 * Подходит для разработки и тестов; данные не сохраняются между перезапусками.
 */
@Slf4j
@Component("inMemoryUserStorage")
@Qualifier("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    /**
     * Хранилище пользователей в памяти приложения, ключ — идентификатор пользователя.
     */
    private final Map<Long, User> users = new HashMap<>();

    /**
     * Счётчик для выдачи уникальных идентификаторов пользователей.
     */
    private final AtomicLong idCounter = new AtomicLong(0);

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User add(User user) {
        useLoginAsNameIfBlank(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User findById(Long id) {
        User user = users.get(id);
        if (user == null) {
            log.warn("Пользователь с id = {} не найден", id);
            throw new NotFoundException("пользователь с id = " + id + " не найден");
        }
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        users.remove(id);
    }

    /**
     * {@inheritDoc}
     * <p>Если имя в обновляемых данных пустое, в качестве имени подставляется логин.
     * Ранее добавленные друзья из существующей записи переносятся в обновлённую,
     * чтобы PUT не сбрасывал список друзей.
     */
    @Override
    public User modify(User user) {
        log.info("Вызван метод для обновления пользователя: {}", user);
        if (user.getId() == null) {
            log.warn("Ошибка обновления пользователя: id не указан");
            throw new ValidateException("id должен быть указан");
        }

        User existing = users.get(user.getId());
        if (existing == null) {
            log.warn("Пользователь с id = {} не найден", user.getId());
            throw new NotFoundException("пользователь с id = " + user.getId() + " не найден");
        }
        useLoginAsNameIfBlank(user);
        user.getFriends().addAll(existing.getFriends());
        users.put(user.getId(), user);
        log.info("Пользователь с id = {} обновлён", user.getId());

        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = findById(userId);
        findById(friendId);
        user.getFriends().add(friendId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFriend(Long userId, Long friendId) {
        User user = findById(userId);
        findById(friendId);
        user.getFriends().remove(friendId);
    }

    /**
     * Генерирует следующий уникальный идентификатор пользователя
     * с помощью атомарного счётчика.
     *
     * @return новый уникальный идентификатор
     */
    private long getNextId() {
        return idCounter.incrementAndGet();
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
