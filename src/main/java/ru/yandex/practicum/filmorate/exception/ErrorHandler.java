package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений REST-контроллеров.
 * Преобразует доменные исключения и ошибки Bean Validation
 * в соответствующие HTTP-ответы с описанием причины.
 */
@RestControllerAdvice
public class ErrorHandler {

    /**
     * Обрабатывает ошибки валидации, выброшенные вручную в контроллере.
     *
     * @param e исключение валидации
     * @return ответ со статусом 400 и описанием ошибки
     */
    @ExceptionHandler(ValidateException.class)
    public ResponseEntity<Map<String, String>> handleValidate(ValidateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
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
    public ResponseEntity<Map<String, String>> handleBeanValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", message));
    }

    /**
     * Обрабатывает ошибки отсутствия запрашиваемой сущности.
     *
     * @param e исключение отсутствия сущности
     * @return ответ со статусом 404 и описанием ошибки
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * Обрабатывает прочие непредвиденные исключения,
     * не покрытые специализированными обработчиками выше.
     *
     * @param e любое необработанное исключение
     * @return ответ со статусом 500 и описанием ошибки
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Map<String, String>> handleAny(Throwable e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
    }
}
