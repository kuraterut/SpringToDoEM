package com.emobile.springtodo.integration;


import com.emobile.springtodo.model.Todo;
import com.emobile.springtodo.repository.TodoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TodoRepositoryIntegrationTest {
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
            .withInitScript("sql/init-test-db.sql");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:latest")
            .withExposedPorts(6379).withStartupTimeout(Duration.ofMinutes(2))
            .withReuse(true);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private TodoRepository todoRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM todos");
    }

    @Test
    @DisplayName("save() - Should save and return todo with generated ID")
    void save_ShouldReturnTodoWithGeneratedId() {
        Todo todo = new Todo();
        todo.setTitle("Test Todo");
        todo.setDescription("Test Description");
        todo.setCompleted(false);
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());

        Todo savedTodo = todoRepository.save(todo);

        assertThat(savedTodo.getId()).isNotNull();
        assertThat(savedTodo.getTitle()).isEqualTo("Test Todo");
    }

    @Test
    @DisplayName("findAll() - Should return paginated todos")
    void findAll_ShouldReturnPaginatedTodos() {
        jdbcTemplate.update("INSERT INTO todos (title, description, completed, created_at, updated_at) VALUES " +
                        "(?, ?, ?, ?, ?), (?, ?, ?, ?, ?), (?, ?, ?, ?, ?)",
                "Todo 1", "Desc 1", false, LocalDateTime.now(), LocalDateTime.now(),
                "Todo 2", "Desc 2", true, LocalDateTime.now(), LocalDateTime.now(),
                "Todo 3", "Desc 3", false, LocalDateTime.now(), LocalDateTime.now());

        List<Todo> todos = todoRepository.findAll(2, 1);

        assertThat(todos).hasSize(2);
        assertThat(todos.get(0)).isNotNull();
    }

    @Test
    @DisplayName("findById() - Should return todo when exists")
    void findById_ShouldReturnTodoWhenExists() {
        Long id = jdbcTemplate.queryForObject(
                "INSERT INTO todos (title, description, completed, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?) RETURNING id",
                Long.class,
                "Test Todo", "Test Desc", false, LocalDateTime.now(), LocalDateTime.now());

        Optional<Todo> foundTodo = todoRepository.findById(id);

        assertThat(foundTodo).isPresent();
        assertThat(foundTodo.get().getTitle()).isEqualTo("Test Todo");
    }

    @Test
    @DisplayName("deleteById() - Should delete todo")
    void deleteById_ShouldDeleteTodo() {
        Long id = jdbcTemplate.queryForObject(
                "INSERT INTO todos (title, description, completed, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?) RETURNING id",
                Long.class,
                "Test Todo", "Test Desc", false, LocalDateTime.now(), LocalDateTime.now());

        todoRepository.deleteById(id);

        Optional<Todo> deletedTodo = todoRepository.findById(id);
        assertThat(deletedTodo).isEmpty();
    }
}