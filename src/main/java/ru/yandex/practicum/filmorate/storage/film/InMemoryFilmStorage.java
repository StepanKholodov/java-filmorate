package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Реализация {@link FilmStorage}, хранящая фильмы в оперативной памяти приложения.
 * Используется как Spring-бин с уникальным жизненным циклом приложения (singleton).
 * Подходит для разработки и тестов; данные не сохраняются между перезапусками.
 */
@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    /**
     * Хранилище фильмов в памяти приложения, ключ — идентификатор фильма.
     */
    private final Map<Long, Film> films = new HashMap<>();

    /**
     * Счётчик для выдачи уникальных идентификаторов фильмов.
     */
    private final AtomicLong idCounter = new AtomicLong(0);

    /**
     * {@inheritDoc}
     */
    @Override
    public Film add(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Film findById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            log.warn("Фильм с id = {} не найден", id);
            throw new NotFoundException("фильм с id = " + id + " не найден");
        }
        return film;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с таким id не найден");
        }
        films.remove(id);
    }

    /**
     * {@inheritDoc}
     * <p>Лайки управляются отдельными эндпоинтами и не принимаются в теле PUT-запроса:
     * набор лайков обновлённой записи полностью берётся из существующего фильма,
     * любые значения поля {@code likes} из запроса игнорируются.
     */
    @Override
    public Film modify(Film film) {
        if (film.getId() == null) {
            log.warn("Ошибка обновления фильма: id не указан");
            throw new ValidateException("id должен быть указан");
        }

        Film existing = films.get(film.getId());
        if (existing == null) {
            log.warn("Фильм с id = {} не найден", film.getId());
            throw new NotFoundException("фильм с id = " + film.getId() + " не найден");
        }

        film.getLikes().clear();
        film.getLikes().addAll(existing.getLikes());
        films.put(film.getId(), film);
        log.info("Фильм с id = {} обновлён", film.getId());

        return film;
    }

    /**
     * Генерирует следующий уникальный идентификатор фильма
     * с помощью атомарного счётчика.
     *
     * @return новый уникальный идентификатор
     */
    private long getNextId() {
        return idCounter.incrementAndGet();
    }
}
