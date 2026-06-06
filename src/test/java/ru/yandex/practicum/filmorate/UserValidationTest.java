package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты Bean Validation на модели {@link User}.
 * Используют стандартный {@link Validator} и проверяют, что аннотации
 * на полях срабатывают и не срабатывают на граничных значениях.
 */
class UserValidationTest {

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
    @DisplayName("Валидный пользователь — без нарушений")
    void validUserHasNoViolations() {
        assertTrue(validator.validate(validUser()).isEmpty());
    }

    @Test
    @DisplayName("Email null → нарушение @NotBlank/@Email")
    void nullEmailTriggersViolation() {
        User user = validUser();
        user.setEmail(null);
        assertViolationOn(user, "email");
    }

    @Test
    @DisplayName("Email без @ → нарушение @Email")
    void invalidEmailTriggersViolation() {
        User user = validUser();
        user.setEmail("invalid-email");
        assertViolationOn(user, "email");
    }

    @Test
    @DisplayName("Email пустая строка → нарушение @NotBlank")
    void blankEmailTriggersViolation() {
        User user = validUser();
        user.setEmail("   ");
        assertViolationOn(user, "email");
    }

    @Test
    @DisplayName("Login null → нарушение @NotBlank")
    void nullLoginTriggersViolation() {
        User user = validUser();
        user.setLogin(null);
        assertViolationOn(user, "login");
    }

    @Test
    @DisplayName("Login с пробелом → нарушение @Pattern")
    void loginWithSpaceTriggersViolation() {
        User user = validUser();
        user.setLogin("bad login");
        assertViolationOn(user, "login");
    }

    @Test
    @DisplayName("Имя null → нарушений нет (поле необязательное, подставится логин)")
    void nullNameIsAccepted() {
        User user = validUser();
        user.setName(null);
        assertTrue(validator.validate(user).isEmpty());
    }

    @Test
    @DisplayName("Birthday null → нарушение @NotNull")
    void nullBirthdayTriggersViolation() {
        User user = validUser();
        user.setBirthday(null);
        assertViolationOn(user, "birthday");
    }

    @Test
    @DisplayName("Birthday в будущем → нарушение @PastOrPresent")
    void futureBirthdayTriggersViolation() {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        assertViolationOn(user, "birthday");
    }

    @Test
    @DisplayName("Birthday сегодня → нарушений нет (граничное значение)")
    void todayBirthdayIsAccepted() {
        User user = validUser();
        user.setBirthday(LocalDate.now());
        assertTrue(validator.validate(user).isEmpty());
    }

    private User validUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("user123");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private void assertViolationOn(User user, String fieldName) {
        boolean hasViolation = validator.validate(user).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(fieldName));
        assertTrue(hasViolation, "Ожидалось нарушение на поле " + fieldName);
    }
}
