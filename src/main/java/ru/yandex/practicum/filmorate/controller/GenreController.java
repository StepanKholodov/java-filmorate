package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

/**
 * REST-контроллер для чтения справочника жанров.
 * Принимает HTTP-запросы по пути {@code /genres}.
 */
@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    /**
     * Возвращает все жанры.
     *
     * @return коллекция всех жанров
     */
    @GetMapping
    public Collection<Genre> findAll() {
        log.info("GET /genres");
        return genreService.getAll();
    }

    /**
     * Возвращает жанр по его идентификатору.
     *
     * @param id идентификатор жанра
     * @return найденный жанр
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если жанр не найден
     */
    @GetMapping("/{id}")
    public Genre getGenre(@PathVariable long id) {
        log.info("GET /genres/{}", id);
        return genreService.getById(id);
    }
}
