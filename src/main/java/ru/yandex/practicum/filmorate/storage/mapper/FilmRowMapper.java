package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Преобразует строку результата запроса (join {@code films} и {@code mpa_ratings})
 * в объект {@link Film}. Жанры и лайки в маппер не входят — они хранятся в отдельных
 * таблицах {@code film_genres} и {@code likes} и подгружаются DAO отдельными запросами.
 */
public class FilmRowMapper implements RowMapper<Film> {

    /**
     * Собирает {@link Film} из текущей строки результата запроса.
     *
     * @param rs     результат запроса, установленный на нужную строку
     * @param rowNum номер строки (не используется)
     * @return фильм с заполненными собственными полями и рейтингом MPA
     * @throws SQLException если чтение данных из результата запроса завершилось ошибкой
     */
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        Date releaseDate = rs.getDate("release_date");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }
        film.setDuration(rs.getInt("duration"));

        Mpa mpa = new Mpa();
        mpa.setId(rs.getLong("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        film.setMpa(mpa);

        return film;
    }
}
