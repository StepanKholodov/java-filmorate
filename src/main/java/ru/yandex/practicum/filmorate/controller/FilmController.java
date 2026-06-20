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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

/**
 * REST-контроллер для работы с фильмами.
 * Принимает HTTP-запросы по пути {@code /films}, выполняет валидацию входных данных
 * аннотацией {@link Valid} и делегирует всю бизнес-логику в {@link FilmService}.
 */
@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    /**
     * Сервис, реализующий операции над фильмами и лайками.
     */
    private final FilmService filmService;

    /**
     * Возвращает все фильмы, хранящиеся в приложении.
     *
     * @return коллекция всех фильмов (может быть пустой)
     */
    @GetMapping
    public Collection<Film> findAll() {
        log.info("GET /films");
        return filmService.getAll();
    }

    /**
     * Возвращает фильм по его уникальному идентификатору.
     *
     * @param id идентификатор фильма
     * @return найденный фильм
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм не найден
     */
    @GetMapping("/{id}")
    public Film getFilm(@PathVariable long id) {
        log.info("GET /films/{}", id);
        return filmService.getById(id);
    }

    /**
     * Создаёт новый фильм. Идентификатор присваивается автоматически.
     *
     * @param newFilm данные нового фильма; проходят Bean Validation и проверку даты релиза
     * @return созданный фильм с присвоенным {@code id}
     * @throws ru.yandex.practicum.filmorate.exception.ValidateException если дата релиза раньше 28.12.1895
     */
    @PostMapping
    public Film create(@Valid @RequestBody Film newFilm) {
        log.info("POST /films {}", newFilm);
        Film created = filmService.create(newFilm);
        log.info("Фильм добавлен с id = {}", created.getId());
        return created;
    }

    /**
     * Обновляет существующий фильм. Идентификатор фильма обязателен.
     * Ранее проставленные лайки сохраняются.
     *
     * @param newFilm данные фильма с указанным {@code id}
     * @return обновлённый фильм
     * @throws ru.yandex.practicum.filmorate.exception.ValidateException если {@code id} не задан или дата релиза некорректна
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм с указанным {@code id} не найден
     */
    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("PUT /films {}", newFilm);
        return filmService.update(newFilm);
    }

    /**
     * Ставит лайк фильму от имени указанного пользователя.
     * Повторный лайк от того же пользователя не дублируется.
     *
     * @param id     идентификатор фильма
     * @param userId идентификатор пользователя, ставящего лайк
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм или пользователь не найдены
     */
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        log.info("PUT /films/{}/like/{}", id, userId);
        filmService.addLike(id, userId);
    }

    /**
     * Убирает ранее поставленный пользователем лайк с фильма.
     *
     * @param id     идентификатор фильма
     * @param userId идентификатор пользователя, снимающего лайк
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм или пользователь не найдены
     */
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable long id, @PathVariable long userId) {
        log.info("DELETE /films/{}/like/{}", id, userId);
        filmService.removeLike(id, userId);
    }

    /**
     * Возвращает список наиболее популярных фильмов, отсортированный по количеству лайков по убыванию.
     *
     * @param count максимальное число фильмов в ответе; по умолчанию 10
     * @return список популярных фильмов (не более {@code count} элементов)
     * @throws ru.yandex.practicum.filmorate.exception.ValidateException если {@code count} не положительный
     */
    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.info("GET /films/popular?count={}", count);
        return filmService.getPopular(count);
    }
}
