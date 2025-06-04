package com.emobile.springtodo.integration;

import com.emobile.springtodo.model.Todo;
import com.emobile.springtodo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class TodoRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TodoRepository todoRepository;

    @BeforeEach
    void setUp() {
        todoRepository.deleteAll();
    }

    @Test
    void save_ShouldReturnTodoWithGeneratedId() {
        Todo todo = Todo.builder()
                .title("Test Todo")
                .description("Test Description")
                .completed(false)
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();

        Todo savedTodo = todoRepository.save(todo);

        assertThat(savedTodo.getId()).isNotNull();
        assertThat(savedTodo.getCreatedAt()).isNotNull();
        assertThat(savedTodo.getUpdatedAt()).isNotNull();
    }

    @Test
    void findById_ShouldReturnTodoWhenExists() {
        Todo savedTodo = todoRepository.save(
                Todo.builder()
                        .title("Test Todo")
                        .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                        .updatedAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                        .build());

        Optional<Todo> foundTodo = todoRepository.findById(savedTodo.getId());

        assertThat(foundTodo).isPresent();
        assertThat(foundTodo.get().getTitle()).isEqualTo("Test Todo");
    }

    @Test
    void deleteById_ShouldDeleteTodo() {
        Todo savedTodo = todoRepository.save(
                Todo.builder()
                        .title("Test Todo")
                        .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                        .updatedAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                        .build());

        todoRepository.deleteById(savedTodo.getId());

        assertThat(todoRepository.findById(savedTodo.getId())).isEmpty();
    }
}