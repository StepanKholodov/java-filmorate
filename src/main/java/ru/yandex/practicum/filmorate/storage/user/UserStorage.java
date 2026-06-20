package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User add(User user);

    void remove(Long id);

    User modify(User user);

    Collection<User> getAll();

    User findById(Long id);
}
