package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

/**
 * REST-контроллер для работы с пользователями.
 * Принимает HTTP-запросы по пути {@code /users}, выполняет валидацию входных данных
 * аннотацией {@link Valid} и делегирует всю бизнес-логику в {@link UserService}.
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    /**
     * Сервис, реализующий операции над пользователями и связями дружбы.
     */
    private final UserService userService;

    /**
     * Возвращает всех пользователей, зарегистрированных в приложении.
     *
     * @return коллекция всех пользователей (может быть пустой)
     */
    @GetMapping
    public Collection<User> findAll() {
        log.info("GET /users");
        return userService.getAll();
    }

    /**
     * Возвращает пользователя по его уникальному идентификатору.
     *
     * @param id идентификатор пользователя
     * @return найденный пользователь
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если пользователь не найден
     */
    @GetMapping("/{id}")
    public User getUser(@PathVariable long id) {
        log.info("GET /users/{}", id);
        return userService.getById(id);
    }

    /**
     * Создаёт нового пользователя. Идентификатор присваивается автоматически.
     * Если поле {@code name} пустое, в качестве имени используется {@code login}.
     *
     * @param newUser данные нового пользователя; проходят Bean Validation
     * @return созданный пользователь с присвоенным {@code id}
     */
    @PostMapping
    public User create(@Valid @RequestBody User newUser) {
        log.info("POST /users {}", newUser);
        User created = userService.create(newUser);
        log.info("Пользователь создан с id = {}", created.getId());
        return created;
    }

    /**
     * Обновляет существующего пользователя. Идентификатор пользователя обязателен.
     * Ранее добавленные друзья сохраняются.
     *
     * @param newUser данные пользователя с указанным {@code id}
     * @return обновлённый пользователь
     * @throws ru.yandex.practicum.filmorate.exception.ValidateException если {@code id} не задан
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если пользователь с указанным {@code id} не найден
     */
    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("PUT /users {}", newUser);
        return userService.update(newUser);
    }

    /**
     * Отправляет запрос на добавление в друзья от пользователя {@code id} к {@code friendId}.
     * Связь становится подтверждённой, только если получатель ранее уже отправлял
     * встречный запрос; иначе она остаётся неподтверждённой до ответного добавления.
     *
     * @param id       идентификатор пользователя, отправляющего запрос
     * @param friendId идентификатор пользователя, которого добавляют в друзья
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если кто-то из пользователей не найден
     */
    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("PUT /users/{}/friends/{}", id, friendId);
        userService.addFriend(id, friendId);
    }

    /**
     * Удаляет связь дружбы между двумя пользователями у обеих сторон.
     *
     * @param id       идентификатор первого пользователя
     * @param friendId идентификатор второго пользователя
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если кто-то из пользователей не найден
     */
    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("DELETE /users/{}/friends/{}", id, friendId);
        userService.removeFriend(id, friendId);
    }

    /**
     * Возвращает список друзей указанного пользователя.
     *
     * @param id идентификатор пользователя
     * @return список друзей (может быть пустым)
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если пользователь не найден
     */
    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable long id) {
        log.info("GET /users/{}/friends", id);
        return userService.getFriends(id);
    }

    /**
     * Возвращает список общих друзей двух пользователей.
     *
     * @param id      идентификатор первого пользователя
     * @param otherId идентификатор второго пользователя
     * @return список общих друзей (может быть пустым)
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если кто-то из пользователей не найден
     */
    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        log.info("GET /users/{}/friends/common/{}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}
