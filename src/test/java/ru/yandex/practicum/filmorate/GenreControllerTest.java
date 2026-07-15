package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.GenreController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.storage.genre.InMemoryGenreStorage;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты {@link GenreController} — проверяют, что контроллер и {@link GenreService}
 * корректно отдают справочник жанров и обрабатывают отсутствующий идентификатор.
 */
class GenreControllerTest {

    private GenreController controller;

    @BeforeEach
    void setUp() {
        GenreService genreService = new GenreService(new InMemoryGenreStorage());
        controller = new GenreController(genreService);
    }

    @Test
    @DisplayName("findAll: возвращает весь справочник жанров")
    void findAllReturnsAllGenres() {
        Collection<Genre> genres = controller.findAll();

        assertEquals(6, genres.size());
        assertTrue(genres.stream().anyMatch(g -> g.getId().equals(1L) && "Комедия".equals(g.getName())));
    }

    @Test
    @DisplayName("getGenre: возвращает жанр по существующему id")
    void getGenreReturnsGenreById() {
        Genre genre = controller.getGenre(2L);

        assertEquals(2L, genre.getId());
        assertEquals("Драма", genre.getName());
    }

    @Test
    @DisplayName("getGenre: несуществующий id → NotFoundException")
    void getGenreMissingThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> controller.getGenre(999L));
    }
}
