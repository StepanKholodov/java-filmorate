package ru.yandex.practicum.filmorate.controller.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;

import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений REST-контроллеров.
 * Преобразует доменные исключения и ошибки Bean Validation
 * в соответствующие HTTP-ответы с описанием причины.
 */
@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    /**
     * Обрабатывает ошибки валидации, выброшенные вручную в контроллере.
     *
     * @param e исключение валидации
     * @return ответ со статусом 400 и описанием ошибки
     */
    @ExceptionHandler(ValidateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidate(final ValidateException e) {
        return new ErrorResponse(e.getMessage());
    }

    /**
     * Обрабатывает ошибки, найденные Bean Validation на аннотациях модели
     * при использовании {@code @Valid} в контроллере.
     * Собирает сообщения по всем нарушенным ограничениям.
     *
     * @param e исключение, содержащее результаты валидации
     * @return ответ со статусом 400 и описанием всех ошибок валидации
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBeanValidation(final MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return  new ErrorResponse(message);
    }

    /**
     * Обрабатывает ошибки отсутствия запрашиваемой сущности.
     *
     * @param e исключение отсутствия сущности
     * @return ответ со статусом 404 и описанием ошибки
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    /**
     * Обрабатывает прочие непредвиденные исключения,
     * не покрытые специализированными обработчиками выше.
     * Стектрейс пишется в лог, чтобы причину 500-й ошибки можно было найти
     * даже когда клиенту отдаётся только краткое сообщение.
     *
     * @param e любое необработанное исключение
     * @return ответ со статусом 500 и описанием ошибки
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAny(final Exception e) {
        log.error("Непредвиденная ошибка при обработке запроса", e);
        return new ErrorResponse(e.getMessage());
    }
}
