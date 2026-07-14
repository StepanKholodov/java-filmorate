package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;

/**
 * Реализация {@link FilmStorage}, хранящая фильмы в реляционной базе данных
 * через {@link JdbcTemplate}. Жанры хранятся в таблице {@code film_genres},
 * лайки — в таблице {@code likes}, возрастной рейтинг — в {@code mpa_ratings}.
 * Клиент передаёт только идентификаторы жанров и рейтинга — сама сущность
 * (с названием) собирается DAO при чтении.
 */
@Slf4j
@Repository("filmDbStorage")
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private static final String INSERT_FILM =
            "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                    "WHERE film_id = ?";
    private static final String DELETE_FILM = "DELETE FROM films WHERE film_id = ?";
    private static final String FIND_ALL_FILMS =
            "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa_ratings m ON m.mpa_id = f.mpa_id";
    private static final String FIND_FILM_BY_ID = FIND_ALL_FILMS + " WHERE f.film_id = ?";
    private static final String INSERT_FILM_GENRE =
            "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String FIND_GENRES_BY_FILM =
            "SELECT g.* FROM film_genres fg JOIN genres g ON g.genre_id = fg.genre_id " +
                    "WHERE fg.film_id = ? ORDER BY g.genre_id";
    private static final String FIND_LIKES_BY_FILM = "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String DELETE_LIKES_BY_FILM = "DELETE FROM likes WHERE film_id = ?";
    private static final String INSERT_LIKE =
            "MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film add(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_FILM, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKey().longValue());
        saveGenres(film);
        log.info("Фильм добавлен с id = {}", film.getId());
        return findById(film.getId());
    }

    @Override
    public void remove(Long id) {
        ensureExists(id);
        jdbcTemplate.update(DELETE_LIKES_BY_FILM, id);
        jdbcTemplate.update(DELETE_FILM_GENRES, id);
        jdbcTemplate.update(DELETE_FILM, id);
    }

    @Override
    public Film modify(Film film) {
        if (film.getId() == null) {
            log.warn("Ошибка обновления фильма: id не указан");
            throw new ValidateException("id должен быть указан");
        }
        ensureExists(film.getId());
        jdbcTemplate.update(UPDATE_FILM, film.getName(), film.getDescription(),
                Date.valueOf(film.getReleaseDate()), film.getDuration(), film.getMpa().getId(), film.getId());
        jdbcTemplate.update(DELETE_FILM_GENRES, film.getId());
        saveGenres(film);
        log.info("Фильм с id = {} обновлён", film.getId());
        return findById(film.getId());
    }

    @Override
    public Collection<Film> getAll() {
        List<Film> films = jdbcTemplate.query(FIND_ALL_FILMS, new FilmRowMapper());
        films.forEach(this::loadGenresAndLikes);
        return films;
    }

    @Override
    public Film findById(Long id) {
        Film film = queryFilmById(id);
        loadGenresAndLikes(film);
        return film;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        ensureExists(filmId);
        jdbcTemplate.update(INSERT_LIKE, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        ensureExists(filmId);
        jdbcTemplate.update(DELETE_LIKE, filmId, userId);
    }

    private Film queryFilmById(Long id) {
        try {
            return jdbcTemplate.queryForObject(FIND_FILM_BY_ID, new FilmRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Фильм с id = {} не найден", id);
            throw new NotFoundException("фильм с id = " + id + " не найден");
        }
    }

    private void ensureExists(Long id) {
        queryFilmById(id);
    }

    private void loadGenresAndLikes(Film film) {
        List<Genre> genres = jdbcTemplate.query(FIND_GENRES_BY_FILM, new GenreRowMapper(), film.getId());
        film.getGenres().addAll(genres);
        film.getLikes().addAll(jdbcTemplate.queryForList(FIND_LIKES_BY_FILM, Long.class, film.getId()));
    }

    private void saveGenres(Film film) {
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(INSERT_FILM_GENRE, film.getId(), genre.getId());
        }
    }
}
