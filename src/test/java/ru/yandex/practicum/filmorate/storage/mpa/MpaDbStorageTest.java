package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Интеграционные тесты {@link MpaDbStorage} на резидентной тестовой БД,
 * засеянной справочником из {@code data.sql} (система MPAA, id 1-5).
 */
@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    void getAllReturnsExactlyFiveRatingsInOrder() {
        Collection<Mpa> ratings = mpaStorage.getAll();

        assertThat(ratings).hasSize(5);
        assertThat(ratings).extracting(Mpa::getId).containsExactly(1L, 2L, 3L, 4L, 5L);
        assertThat(ratings).extracting(Mpa::getName).containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void findByIdReturnsRatingWithName() {
        Mpa mpa = mpaStorage.findById(3L);

        assertThat(mpa.getName()).isEqualTo("PG-13");
    }

    @Test
    void findByIdThrowsWhenRatingMissing() {
        assertThatThrownBy(() -> mpaStorage.findById(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
