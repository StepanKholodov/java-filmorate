package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * REST-контроллер для работы с фильмами.
 * Поддерживает операции получения списка, добавления и обновления.
 * Основная валидация выполняется аннотациями Jakarta Bean Validation на модели
 * {@link Film} при использовании {@code @Valid}; дополнительно контроллер проверяет,
 * что дата релиза не раньше {@link #BIRTH_OF_CINEMA}.
 * Данные хранятся в памяти приложения.
 */
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    /**
     * Дата рождения кинематографа — минимально допустимая дата релиза.
     */
    private static final LocalDate BIRTH_OF_CINEMA = LocalDate.of(1895, Month.DECEMBER, 28);

    /**
     * Хранилище фильмов в памяти приложения, ключ — идентификатор фильма.
     */
    private final Map<Long, Film> films = new HashMap<>();

    /**
     * Счётчик для выдачи уникальных идентификаторов фильмов.
     */
    private final AtomicLong idCounter = new AtomicLong(0);

    /**
     * Возвращает все добавленные фильмы.
     *
     * @return коллекция всех фильмов
     */
    @GetMapping
    public Collection<Film> findAll() {
        log.info("Вызван метод для получения всех фильмов");
        return films.values();
    }

    /**
     * Добавляет новый фильм. Поля проверяются Bean Validation,
     * дата релиза дополнительно проверяется на ограничение по дате рождения кино.
     * Идентификатор присваивается автоматически.
     *
     * @param newFilm данные нового фильма
     * @return добавленный фильм с присвоенным идентификатором
     * @throws ValidateException если дата релиза раньше {@link #BIRTH_OF_CINEMA}
     */
    @PostMapping
    public Film create(@Valid @RequestBody Film newFilm) {
        log.info("Вызван метод для добавления фильма: {}", newFilm);
        validateReleaseDate(newFilm);

        newFilm.setId(getNextId());
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм добавлен с id = {}", newFilm.getId());

        return newFilm;
    }

    /**
     * Обновляет существующий фильм. Поля проверяются Bean Validation,
     * дополнительно проверяется ограничение по дате релиза. Требуется указание id.
     *
     * @param newFilm обновлённые данные фильма
     * @return обновлённый фильм
     * @throws ValidateException если не указан id или дата релиза раньше {@link #BIRTH_OF_CINEMA}
     * @throws NotFoundException если фильма с переданным id не существует
     */
    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("Вызван метод для обновления существующего фильма: {}", newFilm);
        if (newFilm.getId() == null) {
            log.warn("Ошибка обновления фильма: id не указан");
            throw new ValidateException("id должен быть указан");
        }

        if (!films.containsKey(newFilm.getId())) {
            log.warn("Фильм с id = {} не найден", newFilm.getId());
            throw new NotFoundException("фильм с id = " + newFilm.getId() + " не найден");
        }

        validateReleaseDate(newFilm);
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм с id = {} обновлён", newFilm.getId());

        return newFilm;
    }

    /**
     * Проверяет, что дата релиза не раньше {@link #BIRTH_OF_CINEMA}.
     * Это ограничение нестандартное и не выражается готовыми аннотациями Bean Validation,
     * поэтому проверяется отдельно.
     *
     * @param film фильм, у которого проверяется дата релиза
     * @throws ValidateException если дата релиза раньше дня рождения кинематографа
     */
    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(BIRTH_OF_CINEMA)) {
            log.warn("Ошибка валидации: некорректная дата релиза {}", film.getReleaseDate());
            throw new ValidateException("дата релиза не может быть раньше 28 декабря 1895 года");
        }
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
