package com.emobile.springtodo.integration;

import com.emobile.springtodo.dto.request.TodoRequest;
import com.emobile.springtodo.model.Todo;
import com.emobile.springtodo.repository.TodoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TodoControllerIntegrationTest {

    private static final LocalDateTime testCreatedTime = LocalDateTime.of(2025, 4, 25, 5, 5);
    private static final LocalDateTime testUpdatedTime = LocalDateTime.of(2025, 4, 25, 5, 5);


    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withInitScript("sql/init-test-db.sql");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:latest")
            .withExposedPorts(6379);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.liquibase.enabled", () -> "false");
    }

    @BeforeEach
    void setUp(){
        todoRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/todos - Create new TODO")
    void createTodo_ShouldReturnCreatedTodo() throws Exception {
        TodoRequest request = new TodoRequest("Test Todo", "Test Description", false);

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Test Todo")));
    }

    @Test
    @DisplayName("GET /api/todos - Get all TODOs")
    void getAllTodos_ShouldReturnTodoList() throws Exception {
        Todo todo = new Todo();
        todo.setTitle("Test Todo");
        todo.setDescription("Test Description");
        todo.setCompleted(false);
        todo.setCreatedAt(testCreatedTime);
        todo.setUpdatedAt(testUpdatedTime);
        todoRepository.save(todo);

        mockMvc.perform(get("/api/todos")
                        .param("limit", "10")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Test Todo")));
    }

    @Test
    @DisplayName("GET /api/todos/{id} - Get TODO by ID")
    void getTodoById_ShouldReturnTodo() throws Exception {
        Todo todo = new Todo();
        todo.setTitle("Test Todo");
        todo.setCompleted(false);
        todo.setCreatedAt(testCreatedTime);
        todo.setUpdatedAt(testUpdatedTime);
        todo = todoRepository.save(todo);

        mockMvc.perform(get("/api/todos/{id}", todo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(todo.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Test Todo")));
    }

    @Test
    @DisplayName("PUT /api/todos/{id} - Update TODO")
    void updateTodo_ShouldReturnUpdatedTodo() throws Exception {
        Todo todo = new Todo();
        todo.setTitle("Old Title");
        todo.setCompleted(true);
        todo.setCreatedAt(testCreatedTime);
        todo.setUpdatedAt(testUpdatedTime);
        todo = todoRepository.save(todo);

        TodoRequest updateRequest = new TodoRequest("Updated Todo", "Updated Description", true);

        mockMvc.perform(put("/api/todos/{id}", todo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Todo")));
    }

    @Test
    @DisplayName("DELETE /api/todos/{id} - Delete TODO")
    void deleteTodo_ShouldReturnNoContent() throws Exception {
        Todo todo = new Todo();
        todo.setTitle("To Delete");
        todo.setCompleted(false);
        todo.setCreatedAt(testCreatedTime);
        todo.setUpdatedAt(testUpdatedTime);
        todo = todoRepository.save(todo);

        mockMvc.perform(delete("/api/todos/{id}", todo.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/todos/{id}", todo.getId()))
                .andExpect(status().isNotFound());
    }
}