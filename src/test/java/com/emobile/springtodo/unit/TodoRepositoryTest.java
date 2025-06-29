package com.emobile.springtodo.unit;

import com.emobile.springtodo.model.Todo;
import com.emobile.springtodo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoRepositoryTest {

    @Mock
    private TodoRepository todoRepository;

    private Todo testTodo;

    @BeforeEach
    void setUp() {
        testTodo = Todo.builder()
                .id(1L)
                .title("Test Todo")
                .description("Test Description")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }


    @Test
    @DisplayName("findById - Should return empty when not found")
    void findById_ShouldReturnEmptyWhenNotFound() {
        when(todoRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Todo> result = todoRepository.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("save - Should return saved todo")
    void save_ShouldReturnSavedTodo() {
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        Todo result = todoRepository.save(testTodo);

        assertThat(result.getId()).isEqualTo(1L);
        verify(todoRepository).save(testTodo);
    }

    @Test
    @DisplayName("existsById - should return true when todo exists")
    void existsById_ShouldReturnTrueWhenExists() {
        when(todoRepository.existsById(1L)).thenReturn(true);

        boolean result = todoRepository.existsById(1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("deleteById - should delete todo")
    void deleteById_ShouldDeleteTodo() {
        doNothing().when(todoRepository).deleteById(1L);

        todoRepository.deleteById(1L);

        verify(todoRepository).deleteById(1L);
    }
}