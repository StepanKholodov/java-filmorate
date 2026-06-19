package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Модель данных фильма.
 * Базовые ограничения полей описаны через аннотации Jakarta Bean Validation
 * и проверяются автоматически при использовании {@code @Valid} в контроллере.
 * Дополнительное ограничение «дата релиза не раньше 28 декабря 1895 года»
 * валидируется отдельно в {@link ru.yandex.practicum.filmorate.controller.FilmController}.
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
}
