package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(long id) {
        return userStorage.findById(id);
    }

    public User create(User user) {
        return userStorage.add(user);
    }

    public User update(User user) {
        return userStorage.modify(user);
    }

    public void addFriend(long userId, long friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public List<User> getFriends(long userId) {
        User user = userStorage.findById(userId);
        return user.getFriends().stream()
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(long userId, long otherUserId) {
        User user = userStorage.findById(userId);
        User other = userStorage.findById(otherUserId);
        Set<Long> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(other.getFriends());
        return commonIds.stream()
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }
}
