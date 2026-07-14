package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Модель жанра фильма. Справочная сущность — хранится в таблице {@code genres}
 * и не создаётся/не изменяется через API, только читается.
 */
@Data
@EqualsAndHashCode(of = "id")
public class Genre {

    /**
     * Уникальный идентификатор жанра.
     */
    @NotNull(message = "идентификатор жанра должен быть указан")
    private Long id;

    /**
     * Название жанра.
     */
    private String name;
}
