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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Genre> getAll() {
        return jdbcTemplate.query(FIND_ALL_GENRES, new GenreRowMapper());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Genre findById(Long id) {
        try {
            return jdbcTemplate.queryForObject(FIND_GENRE_BY_ID, new GenreRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("жанр с id = " + id + " не найден");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Genre> findAllByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT * FROM genres WHERE genre_id IN (" + placeholders + ")";
        List<Genre> found = jdbcTemplate.query(sql, new GenreRowMapper(), ids.toArray());

        Map<Long, Genre> byId = new LinkedHashMap<>();
        found.forEach(genre -> byId.put(genre.getId(), genre));
        for (Long id : ids) {
            if (!byId.containsKey(id)) {
                throw new NotFoundException("жанр с id = " + id + " не найден");
            }
        }
        return found;
    }
}
