package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Интеграционные тесты {@link FilmDbStorage} на резидентной тестовой БД.
 * Идентификаторы жанров и рейтингов (1-6 и 1-5 соответственно) соответствуют
 * справочным данным, засеянным {@code data.sql}.
 * Каждый тест выполняется в собственной транзакции, откатываемой после теста
 * (поведение {@code @JdbcTest} по умолчанию), поэтому тесты не влияют друг на друга.
 */
@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Film newFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("description of " + name);
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(120);
        film.setMpa(mpaRef(3L));
        return film;
    }

    private User newUser(String login) {
        User user = new User();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName("Name " + login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private Mpa mpaRef(long id) {
        Mpa mpa = new Mpa();
        mpa.setId(id);
        return mpa;
    }

    private Mpa mpaRef(long id, String name) {
        Mpa mpa = mpaRef(id);
        mpa.setName(name);
        return mpa;
    }

    private Genre genreRef(long id) {
        Genre genre = new Genre();
        genre.setId(id);
        return genre;
    }

    private Genre genreRef(long id, String name) {
        Genre genre = genreRef(id);
        genre.setName(name);
        return genre;
    }

    @Test
    void addAssignsIdAndPersistsFilmWithMpaAndGenres() {
        // storage.add() больше не обогащает mpa/жанры запросом к БД — их (с именами)
        // подставляет FilmService.resolveMpaAndGenres до вызова storage, поэтому здесь
        // передаём уже разрешённые значения, как это делает сервис
        Film film = newFilm("Inception");
        film.setMpa(mpaRef(3L, "PG-13"));
        film.getGenres().add(genreRef(4L, "Триллер"));
        film.getGenres().add(genreRef(6L, "Боевик"));

        Film created = filmStorage.add(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getMpa()).hasFieldOrPropertyWithValue("id", 3L).hasFieldOrPropertyWithValue("name", "PG-13");
        assertThat(created.getGenres()).extracting(Genre::getId).containsExactlyInAnyOrder(4L, 6L);
    }

    @Test
    void findByIdReturnsPersistedFilm() {
        Film created = filmStorage.add(newFilm("Matrix"));

        Film found = filmStorage.findById(created.getId());

        assertThat(found)
                .hasFieldOrPropertyWithValue("name", "Matrix")
                .hasFieldOrPropertyWithValue("duration", 120);
        assertThat(found.getMpa().getId()).isEqualTo(3L);
    }

    @Test
    void findByIdThrowsWhenFilmMissing() {
        assertThatThrownBy(() -> filmStorage.findById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addLikeAndFindByIdReturnsLike() {
        Film film = filmStorage.add(newFilm("Liked Film"));
        User user = userStorage.add(newUser("liker"));

        filmStorage.addLike(film.getId(), user.getId());

        assertThat(filmStorage.findById(film.getId()).getLikes()).containsExactly(user.getId());
    }

    @Test
    void removeLikeDeletesLike() {
        Film film = filmStorage.add(newFilm("Liked Film"));
        User user = userStorage.add(newUser("liker"));
        filmStorage.addLike(film.getId(), user.getId());

        filmStorage.removeLike(film.getId(), user.getId());

        assertThat(filmStorage.findById(film.getId()).getLikes()).isEmpty();
    }

    @Test
    void modifyUpdatesFieldsAndReplacesGenres() {
        Film created = filmStorage.add(newFilm("Original"));
        created.getGenres().add(genreRef(1L));
        filmStorage.modify(created);

        Film update = new Film();
        update.setId(created.getId());
        update.setName("Updated");
        update.setDescription("new description");
        update.setReleaseDate(LocalDate.of(2015, 3, 3));
        update.setDuration(90);
        update.setMpa(mpaRef(4L));
        update.getGenres().add(genreRef(2L));

        Film modified = filmStorage.modify(update);

        assertThat(modified)
                .hasFieldOrPropertyWithValue("name", "Updated");
        assertThat(modified.getMpa().getId()).isEqualTo(4L);
        assertThat(modified.getGenres()).extracting(Genre::getId).containsExactly(2L);
    }

    @Test
    void modifyThrowsWhenIdIsNull() {
        Film update = newFilm("noid");
        update.setId(null);

        assertThatThrownBy(() -> filmStorage.modify(update))
                .isInstanceOf(ValidateException.class);
    }

    @Test
    void modifyThrowsWhenFilmMissing() {
        Film update = newFilm("ghost");
        update.setId(999L);

        assertThatThrownBy(() -> filmStorage.modify(update))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllReturnsAllPersistedFilms() {
        filmStorage.add(newFilm("First"));
        filmStorage.add(newFilm("Second"));

        Collection<Film> all = filmStorage.getAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void removeDeletesFilm() {
        Film created = filmStorage.add(newFilm("ToRemove"));

        filmStorage.remove(created.getId());

        assertThatThrownBy(() -> filmStorage.findById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void removeThrowsWhenFilmMissing() {
        assertThatThrownBy(() -> filmStorage.remove(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
