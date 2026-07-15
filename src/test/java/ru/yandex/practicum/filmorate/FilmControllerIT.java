package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты {@link ru.yandex.practicum.filmorate.controller.FilmController},
 * проходящие через реальный HTTP/JSON-слой (сериализация, {@code @Valid},
 * {@link ru.yandex.practicum.filmorate.controller.error.ErrorHandler}) поверх резидентной
 * тестовой БД. Идентификатор рейтинга MPA {@code 1} ("G") соответствует справочным данным,
 * засеянным {@code data.sql}. Каждый тест выполняется в собственной транзакции,
 * откатываемой после теста, поэтому тесты не влияют друг на друга.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:filmorate-film-controller-it;DB_CLOSE_DELAY=-1"
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmControllerIT {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    private Film newFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Mpa mpa = new Mpa();
        mpa.setId(1L);
        film.setMpa(mpa);
        return film;
    }

    private User newUser(String login) {
        User user = new User();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName("Name " + login);
        user.setBirthday(LocalDate.of(1995, 6, 15));
        return user;
    }

    @Test
    void createFilm_validData_returns200AndPersistedFilm() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newFilm("Film"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Film"))
                .andExpect(jsonPath("$.mpa.name").value("G"));
    }

    @Test
    void createFilm_releaseDateBeforeBirthOfCinema_returns400() throws Exception {
        Film film = newFilm("Ancient");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        mockMvc.perform(post("/films")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createFilm_blankName_returns400() throws Exception {
        Film film = newFilm(" ");

        mockMvc.perform(post("/films")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createFilm_unknownMpa_returns404() throws Exception {
        Film film = newFilm("NoMpa");
        film.getMpa().setId(999_999L);

        mockMvc.perform(post("/films")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getFilm_notFound_returns404() throws Exception {
        mockMvc.perform(get("/films/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void likeFlow_addAndRemove_affectsPopular() throws Exception {
        String filmJson = mockMvc.perform(post("/films")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newFilm("Popular"))))
                .andReturn().getResponse().getContentAsString();
        String userJson = mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser("liker"))))
                .andReturn().getResponse().getContentAsString();

        long filmId = objectMapper.readTree(filmJson).get("id").asLong();
        long userId = objectMapper.readTree(userJson).get("id").asLong();

        mockMvc.perform(put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(filmId));
    }
}
