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

class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
    }


    @Test
    @DisplayName("Успешное создание фильма с корректными данными")
    void createValidFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A mind-bending thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        Film created = controller.create(film);
        assertNotNull(created.getId());
        assertEquals("Inception", created.getName());
    }

    @Test
    @DisplayName("Создание фильма: имя null → исключение")
    void createFilmWithNullName() {
        Film film = new Film();
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(10);

        assertThrows(ValidateException.class, () -> controller.create(film));
    }

    @Test
    @DisplayName("Создание фильма: имя пустое → исключение")
    void createFilmWithBlankName() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(10);

        assertThrows(ValidateException.class, () -> controller.create(film));
    }

    @Test
    @DisplayName("Создание фильма: описание null → допустимо")
    void createFilmWithNullDescription() {
        Film film = new Film();
        film.setName("Film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(10);

        Film created = controller.create(film);
        assertNull(created.getDescription());
    }

    @Test
    @DisplayName("Создание фильма: описание длиннее 200 символов → исключение")
    void createFilmWithTooLongDescription() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(10);

        assertThrows(ValidateException.class, () -> controller.create(film));
    }

    @Test
    @DisplayName("Создание фильма: описание ровно 200 символов → успех")
    void createFilmWithMaxDescriptionLength() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("A".repeat(200));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(10);

        Film created = controller.create(film);
        assertEquals(200, created.getDescription().length());
    }

    @Test
    @DisplayName("Создание фильма: дата релиза null → исключение")
    void createFilmWithNullReleaseDate() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Desc");
        film.setDuration(10);

        assertThrows(ValidateException.class, () -> controller.create(film));
    }

    @Test
    @DisplayName("Создание фильма: дата релиза раньше 28.12.1895 → исключение")
    void createFilmWithReleaseDateBeforeCinemaBirth() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(1895, Month.DECEMBER, 27));
        film.setDuration(10);

        assertThrows(ValidateException.class, () -> controller.create(film));
    }

    @Test
    @DisplayName("Создание фильма: дата релиза 28.12.1895 → успех")
    void createFilmWithReleaseDateExactlyBirthOfCinema() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(1895, Month.DECEMBER, 28));
        film.setDuration(10);

        Film created = controller.create(film);
        assertEquals(LocalDate.of(1895, Month.DECEMBER, 28), created.getReleaseDate());
    }

    @Test
    @DisplayName("Создание фильма: duration null → исключение")
    void createFilmWithNullDuration() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        assertThrows(ValidateException.class, () -> controller.create(film));
    }

    @Test
    @DisplayName("Создание фильма: duration отрицательный → исключение")
    void createFilmWithNegativeDuration() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-10);

        assertThrows(ValidateException.class, () -> controller.create(film));
    }

    @Test
    @DisplayName("Создание фильма: duration = 0 → исключение")
    void createFilmWithZeroDuration() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        assertThrows(ValidateException.class, () -> controller.create(film));
    }


    @Test
    @DisplayName("Обновление существующего фильма с корректными данными")
    void updateFilmFullReplacement() {
        Film existing = new Film();
        existing.setName("Original");
        existing.setDescription("Original desc");
        existing.setReleaseDate(LocalDate.of(2000, 1, 1));
        existing.setDuration(100);
        controller.create(existing);

        Film update = new Film();
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
    @DisplayName("Обновление без указания id → исключение")
    void updateFilmWithoutId() {
        Film film = new Film();
        film.setName("No ID");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        assertThrows(ValidateException.class, () -> controller.update(film));
    }

    @Test
    @DisplayName("Обновление с несуществующим id → исключение NotFoundException")
    void updateFilmWithNonExistingId() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Ghost");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        assertThrows(NotFoundException.class, () -> controller.update(film));
    }
}
