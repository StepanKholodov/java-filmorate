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

    @Override
    public Film add(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public Film findById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            log.warn("Фильм с id = {} не найден", id);
            throw new NotFoundException("фильм с id = " + id + " не найден");
        }
        return film;
    }

    @Override
    public void remove(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с таким id не найден");
        }
        films.remove(id);

    }

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

