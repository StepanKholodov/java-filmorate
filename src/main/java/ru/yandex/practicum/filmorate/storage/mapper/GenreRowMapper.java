package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Преобразует строку таблицы {@code genres} в объект {@link Genre}.
 */
public class GenreRowMapper implements RowMapper<Genre> {

    /**
     * Собирает {@link Genre} из текущей строки результата запроса.
     *
     * @param rs     результат запроса, установленный на нужную строку
     * @param rowNum номер строки (не используется)
     * @return жанр с заполненными полями
     * @throws SQLException если чтение данных из результата запроса завершилось ошибкой
     */
    @Override
    public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getLong("genre_id"));
        genre.setName(rs.getString("name"));
        return genre;
    }
}
