package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.Collection;

/**
 * Реализация {@link GenreStorage}, читающая справочник жанров
 * из таблицы {@code genres} через {@link JdbcTemplate}.
 */
@Repository("genreDbStorage")
@Qualifier("genreDbStorage")
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private static final String FIND_ALL_GENRES = "SELECT * FROM genres ORDER BY genre_id";
    private static final String FIND_GENRE_BY_ID = "SELECT * FROM genres WHERE genre_id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Genre> getAll() {
        return jdbcTemplate.query(FIND_ALL_GENRES, new GenreRowMapper());
    }

    @Override
    public Genre findById(Long id) {
        try {
            return jdbcTemplate.queryForObject(FIND_GENRE_BY_ID, new GenreRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("жанр с id = " + id + " не найден");
        }
    }
}
