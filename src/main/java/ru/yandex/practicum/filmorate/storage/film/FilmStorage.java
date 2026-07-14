package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

/**
 * Контракт хранилища фильмов.
 * Абстрагирует способ хранения фильмов от сервисного слоя,
 * чтобы реализацию (in-memory, БД и т.п.) можно было заменить без правок в сервисе.
 */
public interface FilmStorage {

    /**
     * Сохраняет новый фильм и присваивает ему уникальный идентификатор.
     *
     * @param film данные нового фильма
     * @return сохранённый фильм с присвоенным {@code id}
     */
    Film add(Film film);

    /**
     * Удаляет фильм по идентификатору.
     *
     * @param id идентификатор удаляемого фильма
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм не найден
     */
    void remove(Long id);

    /**
     * Обновляет существующий фильм. Идентификатор должен быть задан и существовать в хранилище.
     *
     * @param film данные фильма с указанным {@code id}
     * @return обновлённый фильм
     * @throws ru.yandex.practicum.filmorate.exception.ValidateException если {@code id} не задан
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм с указанным {@code id} не найден
     */
    Film modify(Film film);

    /**
     * Возвращает все сохранённые фильмы.
     *
     * @return коллекция всех фильмов (может быть пустой)
     */
    Collection<Film> getAll();

    /**
     * Возвращает фильм по его идентификатору.
     *
     * @param id идентификатор фильма
     * @return найденный фильм
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм не найден
     */
    Film findById(Long id);

    /**
     * Добавляет лайк фильму от указанного пользователя.
     *
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя, ставящего лайк
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм не найден
     */
    void addLike(Long filmId, Long userId);

    /**
     * Удаляет ранее поставленный лайк пользователя с фильма.
     *
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя, снимающего лайк
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм не найден
     */
    void removeLike(Long filmId, Long userId);
}
