package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

/**
 * Сервис для чтения справочника возрастных рейтингов (MPA). MPA — статичные данные,
 * доступные только на чтение через API.
 */
@Service
@RequiredArgsConstructor
public class MpaService {

    @Qualifier("mpaDbStorage")
    private final MpaStorage mpaStorage;

    /**
     * Возвращает все рейтинги.
     *
     * @return коллекция всех рейтингов
     */
    public Collection<Mpa> getAll() {
        return mpaStorage.getAll();
    }

    /**
     * Возвращает рейтинг по его идентификатору.
     *
     * @param id идентификатор рейтинга
     * @return найденный рейтинг
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если рейтинг не найден
     */
    public Mpa getById(long id) {
        return mpaStorage.findById(id);
    }
}
