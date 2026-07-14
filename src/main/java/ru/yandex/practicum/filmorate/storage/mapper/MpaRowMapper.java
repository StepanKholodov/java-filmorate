package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Преобразует строку таблицы {@code mpa_ratings} в объект {@link Mpa}.
 */
public class MpaRowMapper implements RowMapper<Mpa> {

    /**
     * Собирает {@link Mpa} из текущей строки результата запроса.
     *
     * @param rs     результат запроса, установленный на нужную строку
     * @param rowNum номер строки (не используется)
     * @return рейтинг с заполненными полями
     * @throws SQLException если чтение данных из результата запроса завершилось ошибкой
     */
    @Override
    public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getLong("mpa_id"));
        mpa.setName(rs.getString("name"));
        return mpa;
    }
}
