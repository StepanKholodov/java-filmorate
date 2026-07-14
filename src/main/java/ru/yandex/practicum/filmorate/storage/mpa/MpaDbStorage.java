package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;

import java.util.Collection;

/**
 * Реализация {@link MpaStorage}, читающая справочник рейтингов MPA
 * из таблицы {@code mpa_ratings} через {@link JdbcTemplate}.
 */
@Repository("mpaDbStorage")
@Qualifier("mpaDbStorage")
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private static final String FIND_ALL_MPA = "SELECT * FROM mpa_ratings ORDER BY mpa_id";
    private static final String FIND_MPA_BY_ID = "SELECT * FROM mpa_ratings WHERE mpa_id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Mpa> getAll() {
        return jdbcTemplate.query(FIND_ALL_MPA, new MpaRowMapper());
    }

    @Override
    public Mpa findById(Long id) {
        try {
            return jdbcTemplate.queryForObject(FIND_MPA_BY_ID, new MpaRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("рейтинг MPA с id = " + id + " не найден");
        }
    }
}
