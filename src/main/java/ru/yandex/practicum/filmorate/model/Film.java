package ru.yandex.practicum.filmorate.model;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Модель данных фильма.
 * Базовые ограничения полей описаны через аннотации Jakarta Bean Validation
 * и проверяются автоматически при использовании {@code @Valid} в контроллере.
 * Дополнительное ограничение «дата релиза не раньше 28 декабря 1895 года»
 * проверяется в {@link ru.yandex.practicum.filmorate.service.FilmService}.
 * Равенство и хеш-код вычисляются только по идентификатору, чтобы изменение
 * изменяемых полей не нарушало работу с фильмом в коллекциях на основе хеша.
 */
@Data
@EqualsAndHashCode(of = "id")
public class Film {

    /**
     * Уникальный идентификатор фильма.
     */
    private Long id;

    /**
     * Название фильма; не может быть пустым.
     */
    @NotBlank(message = "название фильма не может быть пустым")
    private String name;

    /**
     * Описание фильма; не более 200 символов.
     */
    @Size(max = 200, message = "максимальная длина описания — 200 символов")
    private String description;

    /**
     * Дата выхода фильма в прокат; обязательное поле.
     */
    @NotNull(message = "дата релиза должна быть указана")
    private LocalDate releaseDate;

    /**
     * Продолжительность фильма в минутах; положительное число.
     */
    @NotNull(message = "продолжительность фильма должна быть указана")
    @Positive(message = "продолжительность фильма должна быть положительным числом")
    private Integer duration;

    /**
     * Жанры фильма; у одного фильма может быть несколько жанров.
     * Для создания/обновления достаточно указать {@code id} каждого жанра —
     * название сервис подставит самостоятельно из справочника.
     */
    @Valid
    private final Set<Genre> genres = new LinkedHashSet<>();

    /**
     * Возрастной рейтинг фильма (MPA), определяющий ограничение по возрасту.
     * Для создания/обновления достаточно указать {@code id} рейтинга —
     * название сервис подставит самостоятельно из справочника.
     */
    @NotNull(message = "возрастной рейтинг должен быть указан")
    @Valid
    private Mpa mpa;

    /**
     * Идентификаторы пользователей, поставивших фильму лайк.
     * Использование множества гарантирует, что один пользователь не может лайкнуть фильм дважды.
     */
    private final Set<Long> likes = new HashSet<>();

}
