package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final LocalDate BIRTH_OF_CINEMA = LocalDate.of(1895, Month.DECEMBER, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Вызван метод для получения всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film newFilm) {
        log.info("Вызван метод для добавления фильма: {}", newFilm);
        validate(newFilm);

        newFilm.setId(getNextId());
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм добавлен с id = {}", newFilm.getId());

        return newFilm;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Вызван метод для обновления существующего фильма: {}", newFilm);
        if (newFilm.getId() == null) {
            log.warn("Ошибка обновления фильма: id не указан");
            throw new ValidateException("id должен быть указан");
        }

        if (!films.containsKey(newFilm.getId())) {
            log.warn("Фильм с id = {} не найден", newFilm.getId());
            throw new NotFoundException("фильм с id = " + newFilm.getId() + " не найден");
        }

        validate(newFilm);
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм с id = {} обновлён", newFilm.getId());

        return newFilm;
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации: пустое название фильма");
            throw new ValidateException("название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Ошибка валидации: описание длиннее {} символов", MAX_DESCRIPTION_LENGTH);
            throw new ValidateException("максимальная длина описания — " + MAX_DESCRIPTION_LENGTH + " символов");
        }

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(BIRTH_OF_CINEMA)) {
            log.warn("Ошибка валидации: некорректная дата релиза {}", film.getReleaseDate());
            throw new ValidateException("дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Ошибка валидации: некорректная продолжительность {}", film.getDuration());
            throw new ValidateException("продолжительность фильма должна быть положительным числом");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
