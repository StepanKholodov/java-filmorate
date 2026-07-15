package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация {@link GenreStorage}, хранящая фиксированный справочник жанров
 * в памяти приложения. Используется как альтернатива {@link GenreDbStorage},
 * например, в юнит-тестах, не поднимающих реальную базу данных.
 */
@Component("inMemoryGenreStorage")
@Qualifier("inMemoryGenreStorage")
public class InMemoryGenreStorage implements GenreStorage {

    private final Map<Long, Genre> genres = new LinkedHashMap<>();

    /**
     * Заполняет справочник фиксированным набором жанров.
     */
    public InMemoryGenreStorage() {
        addGenre(1L, "Комедия");
        addGenre(2L, "Драма");
        addGenre(3L, "Мультфильм");
        addGenre(4L, "Триллер");
        addGenre(5L, "Документальный");
        addGenre(6L, "Боевик");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Genre> getAll() {
        return genres.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Genre findById(Long id) {
        Genre genre = genres.get(id);
        if (genre == null) {
            throw new NotFoundException("жанр с id = " + id + " не найден");
        }
        return genre;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Genre> findAllByIds(Collection<Long> ids) {
        List<Genre> result = new ArrayList<>();
        for (Long id : ids) {
            result.add(findById(id));
        }
        return result;
    }

    /**
     * Создаёт жанр с заданными идентификатором и названием и добавляет его в справочник.
     *
     * @param id   идентификатор жанра
     * @param name название жанра
     */
    private void addGenre(Long id, String name) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        genres.put(id, genre);
    }
}
