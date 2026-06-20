package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    /**
     * Дата рождения кинематографа — минимально допустимая дата релиза.
     */
    private static final LocalDate BIRTH_OF_CINEMA = LocalDate.of(1895, Month.DECEMBER, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(long id) {
        return filmStorage.findById(id);
    }

    public Film create(Film film) {
        validateReleaseDate(film);
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        validateReleaseDate(film);
        return filmStorage.modify(film);
    }

    public void addLike(long filmId, long userId) {
        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId);
        film.getLikes().add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(long filmId, long userId) {
        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId);
        film.getLikes().remove(userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopular(int count) {
        if (count <= 0) {
            throw new ValidateException("count должен быть положительным числом");
        }
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Проверяет, что дата релиза не раньше {@link #BIRTH_OF_CINEMA}.
     * Это ограничение нестандартное и не выражается готовыми аннотациями Bean Validation,
     * поэтому проверяется отдельно.
     *
     * @param film фильм, у которого проверяется дата релиза
     * @throws ValidateException если дата релиза раньше дня рождения кинематографа
     */
    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(BIRTH_OF_CINEMA)) {
            log.warn("Ошибка валидации: некорректная дата релиза {}", film.getReleaseDate());
            throw new ValidateException("дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}
