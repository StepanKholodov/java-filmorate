package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;

/**
 * Сервис для чтения справочника жанров. Жанры — статичные данные,
 * доступные только на чтение через API.
 */
@Service
@RequiredArgsConstructor
public class GenreService {

    @Qualifier("genreDbStorage")
    private final GenreStorage genreStorage;

    /**
     * Возвращает все жанры.
     *
     * @return коллекция всех жанров
     */
    public Collection<Genre> getAll() {
        return genreStorage.getAll();
    }

    /**
     * Возвращает жанр по его идентификатору.
     *
     * @param id идентификатор жанра
     * @return найденный жанр
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если жанр не найден
     */
    public Genre getById(long id) {
        return genreStorage.findById(id);
    }
}
