package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

/**
 * REST-контроллер для чтения справочника возрастных рейтингов (MPA).
 * Принимает HTTP-запросы по пути {@code /mpa}.
 */
@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;

    /**
     * Возвращает все рейтинги.
     *
     * @return коллекция всех рейтингов
     */
    @GetMapping
    public Collection<Mpa> findAll() {
        log.info("GET /mpa");
        return mpaService.getAll();
    }

    /**
     * Возвращает рейтинг по его идентификатору.
     *
     * @param id идентификатор рейтинга
     * @return найденный рейтинг
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если рейтинг не найден
     */
    @GetMapping("/{id}")
    public Mpa getMpa(@PathVariable long id) {
        log.info("GET /mpa/{}", id);
        return mpaService.getById(id);
    }
}
