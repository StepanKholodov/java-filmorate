package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.MpaController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.mpa.InMemoryMpaStorage;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты {@link MpaController} — проверяют, что контроллер и {@link MpaService}
 * корректно отдают справочник рейтингов MPA и обрабатывают отсутствующий идентификатор.
 */
class MpaControllerTest {

    private MpaController controller;

    @BeforeEach
    void setUp() {
        MpaService mpaService = new MpaService(new InMemoryMpaStorage());
        controller = new MpaController(mpaService);
    }

    @Test
    @DisplayName("findAll: возвращает ровно пять рейтингов")
    void findAllReturnsExactlyFiveRatings() {
        Collection<Mpa> ratings = controller.findAll();

        assertEquals(5, ratings.size());
        assertTrue(ratings.stream().anyMatch(m -> m.getId().equals(1L) && "G".equals(m.getName())));
    }

    @Test
    @DisplayName("getMpa: возвращает рейтинг по существующему id")
    void getMpaReturnsRatingById() {
        Mpa mpa = controller.getMpa(3L);

        assertEquals(3L, mpa.getId());
        assertEquals("PG-13", mpa.getName());
    }

    @Test
    @DisplayName("getMpa: несуществующий id → NotFoundException")
    void getMpaMissingThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> controller.getMpa(999L));
    }
}
