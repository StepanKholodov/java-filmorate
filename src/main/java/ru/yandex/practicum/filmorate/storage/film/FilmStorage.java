package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film add(Film film);

    void remove(Long id);

    Film modify(Film film);

    Collection<Film> getAll();

    Film findById(Long id);
}
