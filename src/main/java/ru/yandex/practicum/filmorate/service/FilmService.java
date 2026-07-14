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

/**
 * Сервис, инкапсулирующий бизнес-логику над фильмами:
 * создание и обновление с дополнительной валидацией даты релиза,
 * управление лайками и формирование рейтинга популярных фильмов.
 * Зависит от абстракций {@link FilmStorage} и {@link UserStorage},
 * чтобы реализацию хранения можно было заменить без правок сервиса.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    /**
     * Дата рождения кинематографа — минимально допустимая дата релиза.
     */
    private static final LocalDate BIRTH_OF_CINEMA = LocalDate.of(1895, Month.DECEMBER, 28);

    /**
     * Хранилище фильмов, через которое выполняются все операции чтения и записи.
     */
    private final FilmStorage filmStorage;

    /**
     * Хранилище пользователей; используется для проверки существования
     * пользователя при операциях с лайками.
     */
    private final UserStorage userStorage;

    /**
     * Возвращает все фильмы, хранящиеся в приложении.
     *
     * @return коллекция всех фильмов (может быть пустой)
     */
    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    /**
     * Возвращает фильм по его уникальному идентификатору.
     *
     * @param id идентификатор фильма
     * @return найденный фильм
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм не найден
     */
    public Film getById(long id) {
        return filmStorage.findById(id);
    }

    /**
     * Создаёт новый фильм после проверки даты релиза.
     *
     * @param film данные нового фильма
     * @return созданный фильм с присвоенным идентификатором
     * @throws ValidateException если дата релиза раньше {@link #BIRTH_OF_CINEMA}
     */
    public Film create(Film film) {
        validateReleaseDate(film);
        return filmStorage.add(film);
    }

    /**
     * Обновляет существующий фильм после проверки даты релиза.
     *
     * @param film данные фильма с указанным {@code id}
     * @return обновлённый фильм
     * @throws ValidateException если дата релиза некорректна или {@code id} не задан
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм с указанным {@code id} не найден
     */
    public Film update(Film film) {
        validateReleaseDate(film);
        return filmStorage.modify(film);
    }

    /**
     * Добавляет лайк фильму от указанного пользователя.
     * Повторный лайк не дублируется за счёт использования множества.
     *
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм или пользователь не найдены
     */
    public void addLike(long filmId, long userId) {
        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId);
        film.getLikes().add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    /**
     * Удаляет ранее поставленный лайк пользователя с фильма.
     *
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм или пользователь не найдены
     */
    public void removeLike(long filmId, long userId) {
        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId);
        film.getLikes().remove(userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    /**
     * Возвращает список наиболее популярных фильмов по убыванию числа лайков.
     *
     * @param count максимальное число фильмов в результате
     * @return список фильмов размером не более {@code count}
     * @throws ValidateException если {@code count} меньше или равен нулю
     */
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
