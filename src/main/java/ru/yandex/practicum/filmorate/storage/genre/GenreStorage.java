package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;

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

    /**
     * Возвращает жанры по набору идентификаторов одним обращением к хранилищу,
     * вместо поочерёдного вызова {@link #findById(Long)} для каждого идентификатора.
     *
     * @param ids идентификаторы искомых жанров
     * @return найденные жанры (порядок не гарантирован)
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если хотя бы один жанр не найден
     */
    List<Genre> findAllByIds(Collection<Long> ids);
}
