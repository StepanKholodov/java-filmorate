package ru.yandex.practicum.filmorate.exception;

/**
 * Исключение, выбрасываемое при нарушении правил валидации входных данных.
 * Обрабатывается как HTTP 400 Bad Request.
 */
public class ValidateException extends RuntimeException {

    /**
     * Создаёт исключение с сообщением о причине ошибки валидации.
     *
     * @param message описание ошибки валидации
     */
    public ValidateException(String message) {
        super(message);
    }
}
