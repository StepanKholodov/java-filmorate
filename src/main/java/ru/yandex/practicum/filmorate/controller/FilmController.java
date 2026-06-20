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
 * Бизнес-логика делегирована в {@link FilmService}.
 */
@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("GET /films");
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable long id) {
        log.info("GET /films/{}", id);
        return filmService.getById(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film newFilm) {
        log.info("POST /films {}", newFilm);
        Film created = filmService.create(newFilm);
        log.info("Фильм добавлен с id = {}", created.getId());
        return created;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("PUT /films {}", newFilm);
        return filmService.update(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        log.info("PUT /films/{}/like/{}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable long id, @PathVariable long userId) {
        log.info("DELETE /films/{}/like/{}", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.info("GET /films/popular?count={}", count);
        return filmService.getPopular(count);
    }
}
