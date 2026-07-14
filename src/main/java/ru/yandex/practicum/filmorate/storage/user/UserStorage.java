package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

/**
 * Контракт хранилища пользователей.
 * Абстрагирует способ хранения пользователей от сервисного слоя,
 * чтобы реализацию (in-memory, БД и т.п.) можно было заменить без правок в сервисе.
 */
public interface UserStorage {

    /**
     * Сохраняет нового пользователя и присваивает ему уникальный идентификатор.
     * Если имя пользователя не задано, реализация подставляет в него логин.
     *
     * @param user данные нового пользователя
     * @return сохранённый пользователь с присвоенным {@code id}
     */
    User add(User user);

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор удаляемого пользователя
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если пользователь не найден
     */
    void remove(Long id);

    /**
     * Обновляет существующего пользователя. Идентификатор должен быть задан и существовать в хранилище.
     *
     * @param user данные пользователя с указанным {@code id}
     * @return обновлённый пользователь
     * @throws ru.yandex.practicum.filmorate.exception.ValidateException если {@code id} не задан
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если пользователь с указанным {@code id} не найден
     */
    User modify(User user);

    /**
     * Возвращает всех сохранённых пользователей.
     *
     * @return коллекция всех пользователей (может быть пустой)
     */
    Collection<User> getAll();

    /**
     * Возвращает пользователя по его идентификатору.
     *
     * @param id идентификатор пользователя
     * @return найденный пользователь
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если пользователь не найден
     */
    User findById(Long id);
}
