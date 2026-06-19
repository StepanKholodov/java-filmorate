package ru.yandex.practicum.filmorate;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты {@link UserController} — проверяют логику, выполняемую самим контроллером
 * (генерация id, обработка отсутствующего id и отсутствующего пользователя,
 * подстановка логина в качестве имени).
 * Валидация полей моделей через Bean Validation проверяется в {@link UserValidationTest}.
 */
class UserControllerTest {

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }

    @Test
    @DisplayName("Успешное создание пользователя со всеми корректными полями")
    void createValidUser() {
        User created = controller.create(validUser());
        assertNotNull(created.getId());
        assertEquals("test@example.com", created.getEmail());
    }

    @Test
    @DisplayName("Создание пользователя: имя пустое → заменяется на login")
    void createUserWithEmptyNameReplacedByLogin() {
        User user = validUser();
        user.setName("");

        User created = controller.create(user);
        assertEquals(user.getLogin(), created.getName());
    }

    @Test
    @DisplayName("Создание пользователя: имя null → заменяется на login")
    void createUserWithNullNameReplacedByLogin() {
        User user = validUser();
        user.setName(null);

        User created = controller.create(user);
        assertEquals(user.getLogin(), created.getName());
    }

    @Test
    @DisplayName("Обновление существующего пользователя с корректными данными")
    void updateUserFullReplacement() {
        User original = controller.create(validUser());

        User update = validUser();
        update.setId(original.getId());
        update.setEmail("new@mail.com");
        update.setLogin("newLogin");
        update.setName("New Name");
        update.setBirthday(LocalDate.of(2000, 1, 1));

        User updated = controller.update(update);
        assertEquals("new@mail.com", updated.getEmail());
        assertEquals("newLogin", updated.getLogin());
        assertEquals("New Name", updated.getName());
        assertEquals(LocalDate.of(2000, 1, 1), updated.getBirthday());
    }

    @Test
    @DisplayName("Обновление без указания id → исключение ValidateException")
    void updateUserWithoutId() {
        User user = validUser();
        assertThrows(ValidateException.class, () -> controller.update(user));
    }

    @Test
    @DisplayName("Обновление с несуществующим id → исключение NotFoundException")
    void updateUserWithNonExistingId() {
        User user = validUser();
        user.setId(999L);
        assertThrows(NotFoundException.class, () -> controller.update(user));
    }

    private User validUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("user123");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}
