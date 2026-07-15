package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Интеграционные тесты {@link GenreDbStorage} на резидентной тестовой БД,
 * засеянной справочником из {@code data.sql} (6 жанров, id 1-6).
 */
@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    void getAllReturnsAllSeededGenres() {
        Collection<Genre> genres = genreStorage.getAll();

        assertThat(genres).hasSize(6);
        assertThat(genres).extracting(Genre::getId).containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
    }

    @Test
    void findByIdReturnsGenreWithName() {
        Genre genre = genreStorage.findById(1L);

        assertThat(genre.getName()).isEqualTo("Комедия");
    }

    @Test
    void findByIdThrowsWhenGenreMissing() {
        assertThatThrownBy(() -> genreStorage.findById(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
