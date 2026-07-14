package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты {@link FilmController} — проверяют логику, выполняемую самим контроллером
 * (генерация id, обработка отсутствующего id, отсутствующего фильма
 * и ограничение по дате рождения кинематографа), а также бизнес-логику
 * {@link FilmService}: лайки, топ популярных и сохранение лайков при PUT.
 * Валидация полей моделей через Bean Validation проверяется в {@link FilmValidationTest}.
 */
class FilmControllerTest {

    private FilmController controller;
    private InMemoryUserStorage userStorage;

    @BeforeEach
    void setUp() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        FilmService filmService = new FilmService(filmStorage, userStorage);
        controller = new FilmController(filmService);
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

    @Test
    @DisplayName("Обновление фильма: ранее проставленные лайки сохраняются")
    void updatePreservesLikes() {
        Film film = controller.create(validFilm());
        User user = userStorage.add(validUser());
        controller.addLike(film.getId(), user.getId());

        Film update = validFilm();
        update.setId(film.getId());
        update.setName("Renamed");
        controller.update(update);

        Film stored = controller.getFilm(film.getId());
        assertEquals("Renamed", stored.getName());
        assertEquals(1, stored.getLikes().size());
        assertTrue(stored.getLikes().contains(user.getId()));
    }

    @Test
    @DisplayName("addLike: лайк сохраняется в фильме")
    void addLikeStoresUserId() {
        Film film = controller.create(validFilm());
        User user = userStorage.add(validUser());

        controller.addLike(film.getId(), user.getId());

        assertTrue(controller.getFilm(film.getId()).getLikes().contains(user.getId()));
    }

    @Test
    @DisplayName("addLike: один и тот же пользователь не дублирует лайк")
    void addLikeIsIdempotent() {
        Film film = controller.create(validFilm());
        User user = userStorage.add(validUser());

        controller.addLike(film.getId(), user.getId());
        controller.addLike(film.getId(), user.getId());

        assertEquals(1, controller.getFilm(film.getId()).getLikes().size());
    }

    @Test
    @DisplayName("addLike: несуществующий фильм → NotFoundException")
    void addLikeMissingFilm() {
        User user = userStorage.add(validUser());
        assertThrows(NotFoundException.class, () -> controller.addLike(999L, user.getId()));
    }

    @Test
    @DisplayName("addLike: несуществующий пользователь → NotFoundException")
    void addLikeMissingUser() {
        Film film = controller.create(validFilm());
        assertThrows(NotFoundException.class, () -> controller.addLike(film.getId(), 999L));
    }

    @Test
    @DisplayName("removeLike: лайк удаляется")
    void removeLikeRemovesUserId() {
        Film film = controller.create(validFilm());
        User user = userStorage.add(validUser());
        controller.addLike(film.getId(), user.getId());

        controller.removeLike(film.getId(), user.getId());

        assertTrue(controller.getFilm(film.getId()).getLikes().isEmpty());
    }

    @Test
    @DisplayName("removeLike: несуществующий фильм → NotFoundException")
    void removeLikeMissingFilm() {
        User user = userStorage.add(validUser());
        assertThrows(NotFoundException.class, () -> controller.removeLike(999L, user.getId()));
    }

    @Test
    @DisplayName("getPopular: фильмы возвращаются по убыванию лайков и обрезаются по count")
    void getPopularSortsAndLimits() {
        Film a = controller.create(filmNamed("A"));
        Film b = controller.create(filmNamed("B"));
        Film c = controller.create(filmNamed("C"));
        User u1 = userStorage.add(userWithLogin("u1"));
        User u2 = userStorage.add(userWithLogin("u2"));
        User u3 = userStorage.add(userWithLogin("u3"));

        controller.addLike(b.getId(), u1.getId());
        controller.addLike(b.getId(), u2.getId());
        controller.addLike(b.getId(), u3.getId());
        controller.addLike(c.getId(), u1.getId());

        List<Film> top2 = controller.getPopular(2);
        assertEquals(2, top2.size());
        assertEquals(b.getId(), top2.get(0).getId());
        assertEquals(c.getId(), top2.get(1).getId());
        assertFalse(top2.contains(a));
    }

    @Test
    @DisplayName("getPopular: при пустом хранилище возвращается пустой список")
    void getPopularEmpty() {
        assertTrue(controller.getPopular(10).isEmpty());
    }

    @Test
    @DisplayName("getPopular: count <= 0 → ValidateException")
    void getPopularRejectsNonPositiveCount() {
        assertThrows(ValidateException.class, () -> controller.getPopular(0));
        assertThrows(ValidateException.class, () -> controller.getPopular(-1));
    }

    private Film validFilm() {
        return filmNamed("Inception");
    }

    private Film filmNamed(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("A mind-bending thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        return film;
    }

    private User validUser() {
        return userWithLogin("user123");
    }

    private User userWithLogin(String login) {
        User user = new User();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}
