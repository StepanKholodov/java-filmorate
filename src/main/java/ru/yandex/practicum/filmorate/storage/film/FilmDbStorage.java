package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private static final String FIND_POPULAR_FILMS =
            "SELECT f.*, m.name AS mpa_name FROM films f " +
                    "JOIN mpa_ratings m ON m.mpa_id = f.mpa_id " +
                    "LEFT JOIN likes l ON l.film_id = f.film_id " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                    "ORDER BY COUNT(l.user_id) DESC " +
                    "LIMIT ?";
    private static final String INSERT_FILM_GENRE =
            "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String DELETE_LIKES_BY_FILM = "DELETE FROM likes WHERE film_id = ?";
    private static final String INSERT_LIKE =
            "MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    private final JdbcTemplate jdbcTemplate;

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Long id) {
        ensureExists(id);
        jdbcTemplate.update(DELETE_LIKES_BY_FILM, id);
        jdbcTemplate.update(DELETE_FILM_GENRES, id);
        jdbcTemplate.update(DELETE_FILM, id);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Film> getAll() {
        List<Film> films = jdbcTemplate.query(FIND_ALL_FILMS, new FilmRowMapper());
        attachGenresAndLikes(films);
        return films;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Film findById(Long id) {
        Film film = queryFilmById(id);
        attachGenresAndLikes(List.of(film));
        return film;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLike(Long filmId, Long userId) {
        ensureExists(filmId);
        jdbcTemplate.update(INSERT_LIKE, filmId, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLike(Long filmId, Long userId) {
        ensureExists(filmId);
        jdbcTemplate.update(DELETE_LIKE, filmId, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Film> getPopular(int count) {
        List<Film> films = jdbcTemplate.query(FIND_POPULAR_FILMS, new FilmRowMapper(), count);
        attachGenresAndLikes(films);
        return films;
    }

    /**
     * Читает фильм по идентификатору без жанров и лайков.
     *
     * @param id идентификатор фильма
     * @return фильм с заполненными собственными полями и рейтингом MPA
     * @throws NotFoundException если фильм с таким идентификатором не найден
     */
    private Film queryFilmById(Long id) {
        try {
            return jdbcTemplate.queryForObject(FIND_FILM_BY_ID, new FilmRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Фильм с id = {} не найден", id);
            throw new NotFoundException("фильм с id = " + id + " не найден");
        }
    }

    /**
     * Проверяет, что фильм с указанным идентификатором существует.
     *
     * @param id идентификатор фильма
     * @throws NotFoundException если фильм с таким идентификатором не найден
     */
    private void ensureExists(Long id) {
        queryFilmById(id);
    }

    /**
     * Догружает жанры и лайки для всех переданных фильмов двумя запросами
     * (по одному на жанры и на лайки) независимо от количества фильмов,
     * вместо пары запросов на каждый фильм по отдельности.
     */
    private void attachGenresAndLikes(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        List<Long> filmIds = films.stream().map(Film::getId).toList();
        Map<Long, List<Genre>> genresByFilm = findGenresByFilmIds(filmIds);
        Map<Long, List<Long>> likesByFilm = findLikesByFilmIds(filmIds);
        for (Film film : films) {
            film.getGenres().addAll(genresByFilm.getOrDefault(film.getId(), List.of()));
            film.getLikes().addAll(likesByFilm.getOrDefault(film.getId(), List.of()));
        }
    }

    /**
     * Читает жанры сразу для нескольких фильмов одним запросом.
     *
     * @param filmIds идентификаторы фильмов
     * @return жанры, сгруппированные по идентификатору фильма
     */
    private Map<Long, List<Genre>> findGenresByFilmIds(List<Long> filmIds) {
        String sql = "SELECT fg.film_id, g.genre_id, g.name FROM film_genres fg " +
                "JOIN genres g ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id IN (" + inClausePlaceholders(filmIds.size()) + ") " +
                "ORDER BY fg.film_id, g.genre_id";
        Map<Long, List<Genre>> result = new LinkedHashMap<>();
        jdbcTemplate.query(sql, (RowCallbackHandler) rs -> {
            Genre genre = new GenreRowMapper().mapRow(rs, 0);
            result.computeIfAbsent(rs.getLong("film_id"), id -> new ArrayList<>()).add(genre);
        }, filmIds.toArray());
        return result;
    }

    /**
     * Читает лайки сразу для нескольких фильмов одним запросом.
     *
     * @param filmIds идентификаторы фильмов
     * @return идентификаторы поставивших лайк пользователей, сгруппированные по идентификатору фильма
     */
    private Map<Long, List<Long>> findLikesByFilmIds(List<Long> filmIds) {
        String sql = "SELECT film_id, user_id FROM likes WHERE film_id IN ("
                + inClausePlaceholders(filmIds.size()) + ")";
        Map<Long, List<Long>> result = new LinkedHashMap<>();
        jdbcTemplate.query(sql, (RowCallbackHandler) rs ->
                result.computeIfAbsent(rs.getLong("film_id"), id -> new ArrayList<>()).add(rs.getLong("user_id")),
                filmIds.toArray());
        return result;
    }

    /**
     * Формирует строку из {@code count} плейсхолдеров {@code ?} через запятую
     * для использования в SQL-условии {@code IN (...)}.
     *
     * @param count требуемое число плейсхолдеров
     * @return строка плейсхолдеров, разделённых запятой
     */
    private String inClausePlaceholders(int count) {
        return String.join(",", Collections.nCopies(count, "?"));
    }

    /**
     * Сохраняет связи фильма с его жанрами в таблице {@code film_genres}.
     *
     * @param film фильм с уже присвоенным {@code id} и заполненным набором жанров
     */
    private void saveGenres(Film film) {
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(INSERT_FILM_GENRE, film.getId(), genre.getId());
        }
    }
}
