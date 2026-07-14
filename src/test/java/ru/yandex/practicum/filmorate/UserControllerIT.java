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
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Интеграционные тесты {@link ru.yandex.practicum.filmorate.controller.UserController},
 * проходящие через реальный HTTP/JSON-слой (сериализация, {@code @Valid},
 * {@link ru.yandex.practicum.filmorate.controller.error.ErrorHandler}) поверх резидентной
 * тестовой БД. Каждый тест выполняется в собственной транзакции, откатываемой после теста,
 * поэтому тесты не влияют друг на друга.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:filmorate-user-controller-it;DB_CLOSE_DELAY=-1"
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerIT {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    private User newUser(String login) {
        User user = new User();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName("Name " + login);
        user.setBirthday(LocalDate.of(1995, 6, 15));
        return user;
    }

    @Test
    void createUser_validData_returns200AndPersistedUser() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser("john"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.login").value("john"));
    }

    @Test
    void createUser_invalidEmail_returns400WithValidationMessage() throws Exception {
        User invalid = newUser("baduser");
        invalid.setEmail("not-an-email");

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createUser_duplicateEmail_returns409() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser("original"))))
                .andExpect(status().isOk());

        User duplicate = newUser("другой-логин");
        duplicate.setEmail("original@example.com");

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getUser_notFound_returns404() throws Exception {
        mockMvc.perform(get("/users/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void updateUser_missingId_returns400() throws Exception {
        User withoutId = newUser("noid");

        mockMvc.perform(put("/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withoutId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void friendshipFlow_addGetRemove() throws Exception {
        String userJson = mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser("alice"))))
                .andReturn().getResponse().getContentAsString();
        String friendJson = mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser("bob"))))
                .andReturn().getResponse().getContentAsString();

        long userId = objectMapper.readTree(userJson).get("id").asLong();
        long friendId = objectMapper.readTree(friendJson).get("id").asLong();

        mockMvc.perform(put("/users/{id}/friends/{friendId}", userId, friendId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/friends", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(friendId));

        mockMvc.perform(delete("/users/{id}/friends/{friendId}", userId, friendId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/friends", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
