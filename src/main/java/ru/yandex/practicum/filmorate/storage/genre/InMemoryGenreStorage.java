package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.LinkedHashMap;
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

    public InMemoryGenreStorage() {
        addGenre(1L, "Комедия");
        addGenre(2L, "Драма");
        addGenre(3L, "Мультфильм");
        addGenre(4L, "Триллер");
        addGenre(5L, "Документальный");
        addGenre(6L, "Боевик");
    }

    @Override
    public Collection<Genre> getAll() {
        return genres.values();
    }

    @Override
    public Genre findById(Long id) {
        Genre genre = genres.get(id);
        if (genre == null) {
            throw new NotFoundException("жанр с id = " + id + " не найден");
        }
        return genre;
    }

    private void addGenre(Long id, String name) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        genres.put(id, genre);
    }
}
