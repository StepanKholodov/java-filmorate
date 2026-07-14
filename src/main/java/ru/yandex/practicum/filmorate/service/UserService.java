package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис, инкапсулирующий бизнес-логику над пользователями:
 * управление учётными записями и двусторонними связями дружбы.
 * Зависит от абстракции {@link UserStorage}, чтобы реализацию хранения
 * можно было заменить без правок сервиса.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    /**
     * Хранилище пользователей, через которое выполняются все операции чтения и записи.
     */
    private final UserStorage userStorage;

    /**
     * Возвращает всех пользователей, зарегистрированных в приложении.
     *
     * @return коллекция всех пользователей (может быть пустой)
     */
    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    /**
     * Возвращает пользователя по его уникальному идентификатору.
     *
     * @param id идентификатор пользователя
     * @return найденный пользователь
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если пользователь не найден
     */
    public User getById(long id) {
        return userStorage.findById(id);
    }

    /**
     * Создаёт нового пользователя. Если имя не задано,
     * хранилище подставит в него логин.
     *
     * @param user данные нового пользователя
     * @return созданный пользователь с присвоенным идентификатором
     */
    public User create(User user) {
        return userStorage.add(user);
    }

    /**
     * Обновляет существующего пользователя; ранее добавленные друзья сохраняются.
     *
     * @param user данные пользователя с указанным {@code id}
     * @return обновлённый пользователь
     * @throws ru.yandex.practicum.filmorate.exception.ValidateException если {@code id} не задан
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если пользователь с указанным {@code id} не найден
     */
    public User update(User user) {
        return userStorage.modify(user);
    }

    /**
     * Добавляет двустороннюю связь дружбы между двумя пользователями.
     * Подтверждения со стороны второго пользователя не требуется.
     *
     * @param userId   идентификатор первого пользователя
     * @param friendId идентификатор второго пользователя
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если кто-то из пользователей не найден
     */
    public void addFriend(long userId, long friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    /**
     * Удаляет связь дружбы у обоих пользователей.
     *
     * @param userId   идентификатор первого пользователя
     * @param friendId идентификатор второго пользователя
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если кто-то из пользователей не найден
     */
    public void removeFriend(long userId, long friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    /**
     * Возвращает список друзей указанного пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список друзей (может быть пустым)
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если пользователь не найден
     */
    public List<User> getFriends(long userId) {
        User user = userStorage.findById(userId);
        return user.getFriends().stream()
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает список общих друзей двух пользователей —
     * пересечение их списков друзей.
     *
     * @param userId      идентификатор первого пользователя
     * @param otherUserId идентификатор второго пользователя
     * @return список общих друзей (может быть пустым)
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если кто-то из пользователей не найден
     */
    public List<User> getCommonFriends(long userId, long otherUserId) {
        User user = userStorage.findById(userId);
        User other = userStorage.findById(otherUserId);
        Set<Long> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(other.getFriends());
        return commonIds.stream()
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }
}
