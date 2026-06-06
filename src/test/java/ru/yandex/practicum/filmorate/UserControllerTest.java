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

class UserControllerTest {

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }


    @Test
    @DisplayName("Успешное создание пользователя со всеми корректными полями")
    void createValidUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("user123");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = controller.create(user);
        assertNotNull(created.getId());
        assertEquals("test@example.com", created.getEmail());
    }

    @Test
    @DisplayName("Создание пользователя: имя пустое → заменяется на login")
    void createUserWithEmptyNameReplacedByLogin() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setLogin("john_doe");
        user.setName("");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = controller.create(user);
        assertEquals("john_doe", created.getName());
    }

    @Test
    @DisplayName("Создание пользователя: имя null → заменяется на login")
    void createUserWithNullNameReplacedByLogin() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setLogin("john_doe");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = controller.create(user);
        assertEquals("john_doe", created.getName());
    }

    @Test
    @DisplayName("Создание пользователя: email null → исключение")
    void createUserWithNullEmail() {
        User user = new User();
        user.setLogin("user");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidateException.class, () -> controller.create(user));
    }

    @Test
    @DisplayName("Создание пользователя: email без @ → исключение")
    void createUserWithInvalidEmail() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("user");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidateException.class, () -> controller.create(user));
    }

    @Test
    @DisplayName("Создание пользователя: login null → исключение")
    void createUserWithNullLogin() {
        User user = new User();
        user.setEmail("x@y.com");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidateException.class, () -> controller.create(user));
    }

    @Test
    @DisplayName("Создание пользователя: login содержит пробел → исключение")
    void createUserWithLoginContainingSpace() {
        User user = new User();
        user.setEmail("x@y.com");
        user.setLogin("bad login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidateException.class, () -> controller.create(user));
    }

    @Test
    @DisplayName("Создание пользователя: birthday null → исключение")
    void createUserWithNullBirthday() {
        User user = new User();
        user.setEmail("x@y.com");
        user.setLogin("user");
        user.setName("Name");

        assertThrows(ValidateException.class, () -> controller.create(user));
    }

    @Test
    @DisplayName("Создание пользователя: birthday в будущем → исключение")
    void createUserWithFutureBirthday() {
        User user = new User();
        user.setEmail("x@y.com");
        user.setLogin("user");
        user.setName("Name");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidateException.class, () -> controller.create(user));
    }

    @Test
    @DisplayName("Создание пользователя: birthday сегодня → допустимо")
    void createUserWithBirthdayToday() {
        User user = new User();
        user.setEmail("x@y.com");
        user.setLogin("user");
        user.setName("Name");
        user.setBirthday(LocalDate.now());

        assertDoesNotThrow(() -> controller.create(user));
    }


    @Test
    @DisplayName("Обновление существующего пользователя с корректными данными")
    void updateUserFullReplacement() {
        User original = new User();
        original.setEmail("orig@mail.com");
        original.setLogin("origLogin");
        original.setName("Orig Name");
        original.setBirthday(LocalDate.of(1995, 5, 5));
        controller.create(original);

        User update = new User();
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
    @DisplayName("Обновление без указания id → исключение")
    void updateUserWithoutId() {
        User user = new User();
        user.setEmail("x@y.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidateException.class, () -> controller.update(user));
    }

    @Test
    @DisplayName("Обновление с несуществующим id → исключение NotFoundException")
    void updateUserWithNonExistingId() {
        User user = new User();
        user.setId(999L);
        user.setEmail("x@y.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(NotFoundException.class, () -> controller.update(user));
    }
}
