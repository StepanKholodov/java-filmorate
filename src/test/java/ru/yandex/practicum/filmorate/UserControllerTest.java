package ru.yandex.practicum.filmorate;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты {@link UserController} — проверяют логику, выполняемую самим контроллером
 * (генерация id, обработка отсутствующего id и отсутствующего пользователя,
 * подстановка логина в качестве имени), а также бизнес-логику
 * {@link UserService}: добавление/удаление друзей, общие друзья и сохранение
 * списка друзей при PUT.
 * Валидация полей моделей через Bean Validation проверяется в {@link UserValidationTest}.
 */
class UserControllerTest {

    private UserController controller;

    @BeforeEach
    void setUp() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        controller = new UserController(userService);
    }

    @Test
    @DisplayName("Успешное создание пользователя со всеми корректными полями")
    void createValidUser() {
        User created = controller.create(validUser());
        assertNotNull(created.getId());
        assertEquals("user123@example.com", created.getEmail());
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

    @Test
    @DisplayName("Обновление пользователя: ранее добавленные друзья сохраняются")
    void updatePreservesFriends() {
        User a = controller.create(userWithLogin("a"));
        User b = controller.create(userWithLogin("b"));
        controller.addFriend(a.getId(), b.getId());

        User update = userWithLogin("a");
        update.setId(a.getId());
        update.setName("Renamed");
        controller.update(update);

        User stored = controller.getUser(a.getId());
        assertEquals("Renamed", stored.getName());
        assertEquals(1, stored.getFriends().size());
        assertTrue(stored.getFriends().contains(b.getId()));
    }

    @Test
    @DisplayName("addFriend: дружба становится двусторонней")
    void addFriendIsBidirectional() {
        User a = controller.create(userWithLogin("a"));
        User b = controller.create(userWithLogin("b"));

        controller.addFriend(a.getId(), b.getId());

        assertTrue(controller.getUser(a.getId()).getFriends().contains(b.getId()));
        assertTrue(controller.getUser(b.getId()).getFriends().contains(a.getId()));
    }

    @Test
    @DisplayName("addFriend: несуществующий пользователь → NotFoundException")
    void addFriendMissingUser() {
        User a = controller.create(userWithLogin("a"));
        assertThrows(NotFoundException.class, () -> controller.addFriend(a.getId(), 999L));
        assertThrows(NotFoundException.class, () -> controller.addFriend(999L, a.getId()));
    }

    @Test
    @DisplayName("removeFriend: связь снимается у обоих пользователей")
    void removeFriendRemovesBothSides() {
        User a = controller.create(userWithLogin("a"));
        User b = controller.create(userWithLogin("b"));
        controller.addFriend(a.getId(), b.getId());

        controller.removeFriend(a.getId(), b.getId());

        assertTrue(controller.getUser(a.getId()).getFriends().isEmpty());
        assertTrue(controller.getUser(b.getId()).getFriends().isEmpty());
    }

    @Test
    @DisplayName("getFriends: возвращает список друзей пользователя")
    void getFriendsReturnsAllFriends() {
        User a = controller.create(userWithLogin("a"));
        User b = controller.create(userWithLogin("b"));
        User c = controller.create(userWithLogin("c"));
        controller.addFriend(a.getId(), b.getId());
        controller.addFriend(a.getId(), c.getId());

        List<User> friends = controller.getFriends(a.getId());

        assertEquals(2, friends.size());
        assertTrue(friends.contains(b));
        assertTrue(friends.contains(c));
    }

    @Test
    @DisplayName("getFriends: у пользователя без друзей возвращается пустой список")
    void getFriendsEmpty() {
        User a = controller.create(userWithLogin("a"));
        assertTrue(controller.getFriends(a.getId()).isEmpty());
    }

    @Test
    @DisplayName("getCommonFriends: возвращает только общих друзей двух пользователей")
    void getCommonFriendsReturnsIntersection() {
        User a = controller.create(userWithLogin("a"));
        User b = controller.create(userWithLogin("b"));
        User shared = controller.create(userWithLogin("shared"));
        User onlyA = controller.create(userWithLogin("onlyA"));
        User onlyB = controller.create(userWithLogin("onlyB"));

        controller.addFriend(a.getId(), shared.getId());
        controller.addFriend(a.getId(), onlyA.getId());
        controller.addFriend(b.getId(), shared.getId());
        controller.addFriend(b.getId(), onlyB.getId());

        List<User> common = controller.getCommonFriends(a.getId(), b.getId());

        assertEquals(1, common.size());
        assertEquals(shared.getId(), common.get(0).getId());
    }

    @Test
    @DisplayName("getCommonFriends: если общих друзей нет — пустой список")
    void getCommonFriendsEmpty() {
        User a = controller.create(userWithLogin("a"));
        User b = controller.create(userWithLogin("b"));
        assertTrue(controller.getCommonFriends(a.getId(), b.getId()).isEmpty());
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
