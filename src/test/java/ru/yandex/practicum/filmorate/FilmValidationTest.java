package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты Bean Validation на модели {@link Film}.
 * Используют стандартный {@link Validator} и проверяют, что аннотации
 * на полях срабатывают и не срабатывают на граничных значениях.
 */
class FilmValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        factory.close();
    }

    @Test
    @DisplayName("Валидный фильм — без нарушений")
    void validFilmHasNoViolations() {
        assertTrue(validator.validate(validFilm()).isEmpty());
    }

    @Test
    @DisplayName("Имя null → нарушение @NotBlank")
    void nullNameTriggersViolation() {
        Film film = validFilm();
        film.setName(null);
        assertViolationOn(film, "name");
    }

    @Test
    @DisplayName("Имя из пробелов → нарушение @NotBlank")
    void blankNameTriggersViolation() {
        Film film = validFilm();
        film.setName("   ");
        assertViolationOn(film, "name");
    }

    @Test
    @DisplayName("Описание длиннее 200 символов → нарушение @Size")
    void tooLongDescriptionTriggersViolation() {
        Film film = validFilm();
        film.setDescription("A".repeat(201));
        assertViolationOn(film, "description");
    }

    @Test
    @DisplayName("Описание ровно 200 символов → нарушений нет (граничное значение)")
    void maxLengthDescriptionIsAccepted() {
        Film film = validFilm();
        film.setDescription("A".repeat(200));
        assertTrue(validator.validate(film).isEmpty());
    }

    @Test
    @DisplayName("Описание null → нарушений нет (поле необязательное)")
    void nullDescriptionIsAccepted() {
        Film film = validFilm();
        film.setDescription(null);
        assertTrue(validator.validate(film).isEmpty());
    }

    @Test
    @DisplayName("Дата релиза null → нарушение @NotNull")
    void nullReleaseDateTriggersViolation() {
        Film film = validFilm();
        film.setReleaseDate(null);
        assertViolationOn(film, "releaseDate");
    }

    @Test
    @DisplayName("Продолжительность null → нарушение @NotNull")
    void nullDurationTriggersViolation() {
        Film film = validFilm();
        film.setDuration(null);
        assertViolationOn(film, "duration");
    }

    @Test
    @DisplayName("Продолжительность 0 → нарушение @Positive")
    void zeroDurationTriggersViolation() {
        Film film = validFilm();
        film.setDuration(0);
        assertViolationOn(film, "duration");
    }

    @Test
    @DisplayName("Отрицательная продолжительность → нарушение @Positive")
    void negativeDurationTriggersViolation() {
        Film film = validFilm();
        film.setDuration(-10);
        assertViolationOn(film, "duration");
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("Short");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        return film;
    }

    private void assertViolationOn(Film film, String fieldName) {
        boolean hasViolation = validator.validate(film).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(fieldName));
        assertTrue(hasViolation, "Ожидалось нарушение на поле " + fieldName);
    }
}
