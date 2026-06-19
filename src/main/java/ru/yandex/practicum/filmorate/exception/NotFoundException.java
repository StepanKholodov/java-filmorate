package ru.yandex.practicum.filmorate.exception;

/**
 * Исключение, выбрасываемое, когда запрашиваемая сущность не найдена.
 * Обрабатывается как HTTP 404 Not Found.
 */
public class NotFoundException extends RuntimeException {

    /**
     * Создаёт исключение с сообщением о том, какая сущность не найдена.
     *
     * @param message описание того, что не было найдено
     */
    public NotFoundException(String message) {
        super(message);
    }
}
