package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Реализация {@link MpaStorage}, хранящая фиксированный справочник рейтингов MPA
 * (система MPAA) в памяти приложения. Используется как альтернатива
 * {@link MpaDbStorage}, например, в юнит-тестах, не поднимающих реальную базу данных.
 */
@Component("inMemoryMpaStorage")
@Qualifier("inMemoryMpaStorage")
public class InMemoryMpaStorage implements MpaStorage {

    private final Map<Long, Mpa> ratings = new LinkedHashMap<>();

    public InMemoryMpaStorage() {
        addRating(1L, "G");
        addRating(2L, "PG");
        addRating(3L, "PG-13");
        addRating(4L, "R");
        addRating(5L, "NC-17");
    }

    @Override
    public Collection<Mpa> getAll() {
        return ratings.values();
    }

    @Override
    public Mpa findById(Long id) {
        Mpa mpa = ratings.get(id);
        if (mpa == null) {
            throw new NotFoundException("рейтинг MPA с id = " + id + " не найден");
        }
        return mpa;
    }

    private void addRating(Long id, String name) {
        Mpa mpa = new Mpa();
        mpa.setId(id);
        mpa.setName(name);
        ratings.put(id, mpa);
    }
}
