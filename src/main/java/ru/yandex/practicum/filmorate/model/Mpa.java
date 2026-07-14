package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Модель возрастного рейтинга фильма (MPA). Справочная сущность — хранится
 * в таблице {@code mpa_ratings} и не создаётся/не изменяется через API, только читается.
 */
@Data
@EqualsAndHashCode(of = "id")
public class Mpa {

    /**
     * Уникальный идентификатор рейтинга.
     */
    @NotNull(message = "идентификатор рейтинга должен быть указан")
    private Long id;

    /**
     * Название рейтинга (например, {@code PG-13}).
     */
    private String name;
}
