package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

/**
 * Контракт хранилища жанров. Жанры — справочные данные,
 * доступные только на чтение через API.
 */
public interface GenreStorage {

    /**
     * Возвращает все жанры, отсортированные по идентификатору по возрастанию.
     *
     * @return коллекция всех жанров
     */
    Collection<Genre> getAll();

    /**
     * Возвращает жанр по его идентификатору.
     *
     * @param id идентификатор жанра
     * @return найденный жанр
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если жанр не найден
     */
    Genre findById(Long id);
}
