package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Вызван метод для получения всех пользователей");
        return users.values();
    }


    @PostMapping
    public User create(@RequestBody User newUser) {
        log.info("Вызван метод для создания пользователя: {}", newUser);
        validate(newUser);

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            log.info("Имя пустое, используется логин: {}", newUser.getLogin());
            newUser.setName(newUser.getLogin());
        }

        newUser.setId(getNextId());
        users.put(newUser.getId(), newUser);
        log.info("Пользователь создан с id = {}", newUser.getId());

        return newUser;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.info("Вызван метод для обновления пользователя: {}", newUser);
        if (newUser.getId() == null) {
            log.warn("Ошибка обновления пользователя: id не указан");
            throw new ValidateException("id должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            log.warn("Пользователь с id = {} не найден", newUser.getId());
            throw new NotFoundException("пользователь с id = " + newUser.getId() + " не найден");
        }

        validate(newUser);

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }

        users.put(newUser.getId(), newUser);
        log.info("Пользователь с id = {} обновлён", newUser.getId());

        return newUser;
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Ошибка валидации: некорректный email {}", user.getEmail());
            throw new ValidateException("электронная почта не может быть пустой и должна содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации: некорректный логин {}", user.getLogin());
            throw new ValidateException("логин не может быть пустым и содержать пробелы");
        }

        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации: некорректная дата рождения {}", user.getBirthday());
            throw new ValidateException("дата рождения не может быть в будущем");
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
