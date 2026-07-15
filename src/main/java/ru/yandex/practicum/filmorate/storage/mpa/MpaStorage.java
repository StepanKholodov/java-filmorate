package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

/**
 * Контракт хранилища возрастных рейтингов (MPA). MPA — справочные данные,
 * доступные только на чтение через API.
 */
public interface MpaStorage {

    /**
     * Возвращает все рейтинги, отсортированные по идентификатору по возрастанию.
     *
     * @return коллекция всех рейтингов
     */
    Collection<Mpa> getAll();

    /**
     * Возвращает рейтинг по его идентификатору.
     *
     * @param id идентификатор рейтинга
     * @return найденный рейтинг
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если рейтинг не найден
     */
    Mpa findById(Long id);
}
