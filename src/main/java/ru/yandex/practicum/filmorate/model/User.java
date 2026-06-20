package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Модель данных пользователя.
 * Ограничения полей описаны через аннотации Jakarta Bean Validation
 * и проверяются автоматически при использовании {@code @Valid} в контроллере.
 * Поле {@link #name} ограничений валидации не имеет: если оно не задано,
 * контроллер подставляет значение из {@link #login}.
 * Равенство и хеш-код вычисляются только по идентификатору, чтобы изменение
 * изменяемых полей не нарушало работу с пользователем в коллекциях на основе хеша.
 */
@Data
@EqualsAndHashCode(of = "id")
public class User {

    /**
     * Уникальный идентификатор пользователя.
     */
    private Long id;

    /**
     * Электронная почта пользователя; должна быть валидной и содержать «@».
     */
    @NotBlank(message = "электронная почта не может быть пустой")
    @Email(message = "электронная почта должна быть корректной и содержать символ @")
    private String email;

    /**
     * Логин пользователя; не пустой и без пробелов.
     */
    @NotBlank(message = "логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "логин не может содержать пробелы")
    private String login;

    /**
     * Имя пользователя для отображения; если пустое — используется логин.
     */
    private String name;

    /**
     * Дата рождения пользователя; не может быть в будущем.
     */
    @NotNull(message = "дата рождения должна быть указана")
    @PastOrPresent(message = "дата рождения не может быть в будущем")
    private LocalDate birthday;

    private final Set<Long> friends = new HashSet<>();

}
