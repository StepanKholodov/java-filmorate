package ru.yandex.practicum.filmorate.controller.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Тело ответа об ошибке, отдаваемое клиенту при обработке исключений.
 */
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final String error;
}