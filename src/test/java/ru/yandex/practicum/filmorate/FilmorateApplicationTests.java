package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Проверяет, что весь контекст приложения поднимается без ошибок.
 * Использует резидентную БД вместо файловой из {@code application.properties},
 * чтобы не зависеть от файла {@code db/filmorate.mv.db} на диске
 * (он может быть занят работающим приложением или открыт в клиенте БД).
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:filmorate-context-test;DB_CLOSE_DELAY=-1"
})
class FilmorateApplicationTests {

    @Test
    void contextLoads() {
    }

}
