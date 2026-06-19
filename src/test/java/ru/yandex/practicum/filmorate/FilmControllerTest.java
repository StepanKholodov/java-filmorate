package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты {@link FilmController} — проверяют логику, выполняемую самим контроллером
 * (генерация id, обработка отсутствующего id, отсутствующего фильма
 * и ограничение по дате рождения кинематографа).
 * Валидация полей моделей через Bean Validation проверяется в {@link FilmValidationTest}.
 */
class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
    }

    @Test
    @DisplayName("Успешное создание фильма с корректными данными")
    void createValidFilm() {
        Film created = controller.create(validFilm());
        assertNotNull(created.getId());
        assertEquals("Inception", created.getName());
    }

    @Test
    @DisplayName("Создание фильма: дата релиза раньше 28.12.1895 → исключение")
    void createFilmWithReleaseDateBeforeCinemaBirth() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, Month.DECEMBER, 27));

        assertThrows(ValidateException.class, () -> controller.create(film));
    }

    @Test
    @DisplayName("Создание фильма: дата релиза 28.12.1895 → успех (граничное значение)")
    void createFilmWithReleaseDateExactlyBirthOfCinema() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, Month.DECEMBER, 28));

        Film created = controller.create(film);
        assertEquals(LocalDate.of(1895, Month.DECEMBER, 28), created.getReleaseDate());
    }

    @Test
    @DisplayName("Обновление существующего фильма с корректными данными")
    void updateFilmFullReplacement() {
        Film existing = controller.create(validFilm());

        Film update = validFilm();
        update.setId(existing.getId());
        update.setName("Updated");
        update.setDescription("New desc");
        update.setReleaseDate(LocalDate.of(2010, 5, 5));
        update.setDuration(120);

        Film updated = controller.update(update);
        assertEquals("Updated", updated.getName());
        assertEquals("New desc", updated.getDescription());
        assertEquals(LocalDate.of(2010, 5, 5), updated.getReleaseDate());
        assertEquals(120, updated.getDuration());
    }

    @Test
    @DisplayName("Обновление без указания id → исключение ValidateException")
    void updateFilmWithoutId() {
        Film film = validFilm();
        assertThrows(ValidateException.class, () -> controller.update(film));
    }

    @Test
    @DisplayName("Обновление с несуществующим id → исключение NotFoundException")
    void updateFilmWithNonExistingId() {
        Film film = validFilm();
        film.setId(999L);
        assertThrows(NotFoundException.class, () -> controller.update(film));
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A mind-bending thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        return film;
    }
}
