package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * REST-контроллер для работы с пользователями.
 * Поддерживает операции получения списка, создания и обновления.
 * Валидация полей выполняется аннотациями Jakarta Bean Validation на модели
 * {@link User} при использовании {@code @Valid}; контроллер дополнительно
 * подставляет логин в качестве имени, если имя пустое.
 * Данные хранятся в памяти приложения.
 */
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    /** Хранилище пользователей в памяти приложения, ключ — идентификатор пользователя. */
    private final Map<Long, User> users = new HashMap<>();

    /** Счётчик для выдачи уникальных идентификаторов пользователей. */
    private final AtomicLong idCounter = new AtomicLong(0);

    /**
     * Возвращает всех зарегистрированных пользователей.
     *
     * @return коллекция всех пользователей
     */
    @GetMapping
    public Collection<User> findAll() {
        log.info("Вызван метод для получения всех пользователей");
        return users.values();
    }

    /**
     * Создаёт нового пользователя. Поля проверяются Bean Validation;
     * если имя не задано — используется логин. Идентификатор присваивается автоматически.
     *
     * @param newUser данные нового пользователя
     * @return созданный пользователь с присвоенным идентификатором
     */
    @PostMapping
    public User create(@Valid @RequestBody User newUser) {
        log.info("Вызван метод для создания пользователя: {}", newUser);
        useLoginAsNameIfBlank(newUser);

        newUser.setId(getNextId());
        users.put(newUser.getId(), newUser);
        log.info("Пользователь создан с id = {}", newUser.getId());

        return newUser;
    }

    /**
     * Обновляет данные существующего пользователя. Поля проверяются Bean Validation;
     * если имя не задано — используется логин. Требуется указание id.
     *
     * @param newUser обновлённые данные пользователя
     * @return обновлённый пользователь
     * @throws ValidateException если не указан id
     * @throws NotFoundException если пользователя с переданным id не существует
     */
    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Вызван метод для обновления пользователя: {}", newUser);
        if (newUser.getId() == null) {
            log.warn("Ошибка обновления пользователя: id не указан");
            throw new ValidateException("id должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            log.warn("Пользователь с id = {} не найден", newUser.getId());
            throw new NotFoundException("пользователь с id = " + newUser.getId() + " не найден");
        }

        useLoginAsNameIfBlank(newUser);
        users.put(newUser.getId(), newUser);
        log.info("Пользователь с id = {} обновлён", newUser.getId());

        return newUser;
    }

    /**
     * Если у пользователя не задано имя для отображения,
     * подставляет в него логин.
     *
     * @param user пользователь, у которого может быть пустое имя
     */
    private void useLoginAsNameIfBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пустое, используется логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }

    /**
     * Генерирует следующий уникальный идентификатор пользователя
     * с помощью атомарного счётчика.
     *
     * @return новый уникальный идентификатор
     */
    private long getNextId() {
        return idCounter.incrementAndGet();
    }
}
