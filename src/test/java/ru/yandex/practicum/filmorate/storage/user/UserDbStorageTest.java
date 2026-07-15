package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Интеграционные тесты {@link UserDbStorage} на резидентной тестовой БД.
 * Каждый тест выполняется в собственной транзакции, откатываемой после теста
 * (поведение {@code @JdbcTest} по умолчанию), поэтому тесты не влияют друг на друга.
 */
@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    private User newUser(String login) {
        User user = new User();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName("Name " + login);
        user.setBirthday(LocalDate.of(1995, 6, 15));
        return user;
    }

    @Test
    void addAssignsIdAndPersistsUser() {
        User created = userStorage.add(newUser("john"));

        assertThat(created.getId()).isNotNull();

        User found = userStorage.findById(created.getId());
        assertThat(found)
                .hasFieldOrPropertyWithValue("email", "john@example.com")
                .hasFieldOrPropertyWithValue("login", "john")
                .hasFieldOrPropertyWithValue("name", "Name john")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1995, 6, 15));
    }

    @Test
    void addUsesLoginAsNameWhenNameBlank() {
        User user = newUser("blankname");
        user.setName(" ");

        User created = userStorage.add(user);

        assertThat(created.getName()).isEqualTo("blankname");
    }

    @Test
    void findByIdThrowsWhenUserMissing() {
        assertThatThrownBy(() -> userStorage.findById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findByIdLoadsFriendsFromFriendshipTable() {
        User user = userStorage.add(newUser("withfriend"));
        User friend = userStorage.add(newUser("friend"));
        userStorage.addFriend(user.getId(), friend.getId());

        User found = userStorage.findById(user.getId());

        assertThat(found.getFriends()).containsExactly(friend.getId());
    }

    @Test
    void addFriendIsOneDirectional() {
        User user = userStorage.add(newUser("a"));
        User friend = userStorage.add(newUser("b"));

        userStorage.addFriend(user.getId(), friend.getId());

        assertThat(userStorage.findById(user.getId()).getFriends()).contains(friend.getId());
        assertThat(userStorage.findById(friend.getId()).getFriends()).isEmpty();
    }

    @Test
    void removeFriendDeletesOnlyThatDirection() {
        User user = userStorage.add(newUser("a"));
        User friend = userStorage.add(newUser("b"));
        userStorage.addFriend(user.getId(), friend.getId());

        userStorage.removeFriend(user.getId(), friend.getId());

        assertThat(userStorage.findById(user.getId()).getFriends()).isEmpty();
    }

    @Test
    void modifyUpdatesFieldsAndPreservesId() {
        User created = userStorage.add(newUser("original"));

        User update = new User();
        update.setId(created.getId());
        update.setEmail("changed@example.com");
        update.setLogin("changedlogin");
        update.setName("Changed Name");
        update.setBirthday(LocalDate.of(2000, 1, 1));

        User modified = userStorage.modify(update);

        assertThat(modified)
                .hasFieldOrPropertyWithValue("id", created.getId())
                .hasFieldOrPropertyWithValue("email", "changed@example.com")
                .hasFieldOrPropertyWithValue("login", "changedlogin")
                .hasFieldOrPropertyWithValue("name", "Changed Name");
    }

    @Test
    void modifyThrowsWhenIdIsNull() {
        User update = newUser("noid");
        update.setId(null);

        assertThatThrownBy(() -> userStorage.modify(update))
                .isInstanceOf(ValidateException.class);
    }

    @Test
    void modifyThrowsWhenUserMissing() {
        User update = newUser("ghost");
        update.setId(999L);

        assertThatThrownBy(() -> userStorage.modify(update))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllReturnsAllPersistedUsers() {
        userStorage.add(newUser("a"));
        userStorage.add(newUser("b"));

        Collection<User> all = userStorage.getAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void removeDeletesUser() {
        User created = userStorage.add(newUser("toremove"));

        userStorage.remove(created.getId());

        assertThatThrownBy(() -> userStorage.findById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void removeThrowsWhenUserMissing() {
        assertThatThrownBy(() -> userStorage.remove(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
