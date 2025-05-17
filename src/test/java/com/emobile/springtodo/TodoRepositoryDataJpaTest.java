package com.emobile.springtodo;

import com.emobile.springtodo.model.Todo;
import com.emobile.springtodo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TodoRepository.class)
public class TodoRepositoryDataJpaTest {

    @Autowired
    private TodoRepository todoRepository;

    private Todo testTodo;

    @BeforeEach
    void setUp() {
        testTodo = Todo.builder()
                .title("Test Todo")
                .description("Test Description")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("save() - Should save and return todo with generated ID")
    void save_ShouldPersistTodo() {
        Todo savedTodo = todoRepository.save(testTodo);

        assertThat(savedTodo.getId()).isNotNull();
        assertThat(savedTodo.getTitle()).isEqualTo("Test Todo");
        assertThat(todoRepository.count(5, 0)).isEqualTo(1);
    }

    @Test
    @Sql("/sql/insert-test-todos.sql")
    @DisplayName("findAll() - Should return paginated todos")
    void findAll_ShouldReturnPaginatedTodos() {
        List<Todo> todos = todoRepository.findAll(2, 0);

        assertThat(todos).hasSize(2);
        assertThat(todos.get(0)).isNotNull();
    }

    @Test
    @Sql("/sql/insert-test-todo.sql")
    @DisplayName("findById() - Should return todo when exists")
    void findById_ShouldReturnTodoWhenExists() {
        Optional<Todo> foundTodo = todoRepository.findById(2L);

        assertThat(foundTodo).isPresent();
        assertThat(foundTodo.get().getTitle()).isEqualTo("Test Todo");
    }

    @Test
    @DisplayName("findById() - Should return empty when not found")
    void findById_ShouldReturnEmptyWhenNotFound() {
        Optional<Todo> foundTodo = todoRepository.findById(999L);

        assertThat(foundTodo).isEmpty();
    }

    @Test
    @Sql("/sql/insert-test-todo.sql")
    @DisplayName("deleteById() - Should delete todo")
    void deleteById_ShouldRemoveTodo() {
        Long id = 2L;
        assertThat(todoRepository.findById(id)).isPresent();

        todoRepository.deleteById(id);

        assertThat(todoRepository.findById(id)).isEmpty();
    }

    @Test
    @Sql("/sql/insert-test-todos.sql")
    @DisplayName("findByCompleted() - Should return only completed todos")
    void findByCompleted_ShouldFindCompleted() {
        List<Todo> completedTodos = todoRepository.findByCompleted(5, 0);

        assertThat(completedTodos)
                .hasSize(1)
                .allMatch(Todo::isCompleted);
    }
}