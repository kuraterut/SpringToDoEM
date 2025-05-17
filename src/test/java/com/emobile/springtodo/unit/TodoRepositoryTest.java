package com.emobile.springtodo.unit;

import com.emobile.springtodo.model.Todo;
import com.emobile.springtodo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TodoRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TodoRepository todoRepository;

    private Todo testTodo;

    @BeforeEach
    void setUp() {
        testTodo = new Todo();
        testTodo.setId(1L);
        testTodo.setTitle("Test Todo");
    }

    @Test
    @DisplayName("findAll - Should execute correct SQL query")
    void findAll_ShouldExecuteCorrectQuery() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(10), eq(0)))
                .thenReturn(List.of(testTodo));

        List<Todo> result = todoRepository.findAll(10, 0);

        assertEquals(1, result.size());
        verify(jdbcTemplate, times(1))
                .query(contains("SELECT * FROM todos"), any(RowMapper.class), eq(10), eq(0));
    }

    @Test
    @DisplayName("findById - Should return empty when not found")
    void findById_ShouldReturnEmptyWhenNotFound() {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(1L)))
                .thenReturn(null);

        Optional<Todo> result = todoRepository.findById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("save - Should insert new todo")
    void save_ShouldInsertNewTodo() {
        testTodo.setId(null);
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(), any(), any(), any(), any()))
                .thenReturn(1L);

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(1L)))
                .thenReturn(testTodo);

        Todo result = todoRepository.save(testTodo);

        assertEquals(1L, result.getId());
        verify(jdbcTemplate, times(1))
                .queryForObject(contains("INSERT INTO todos"), eq(Long.class), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("save - Should update todo")
    void save_ShouldUpdateTodo() {
        testTodo.setId(1L);
        when(jdbcTemplate.update(anyString(), eq(Long.class), any(), any(), any(), any(), any())).thenReturn(1);

        Todo result = todoRepository.save(testTodo);

        assertEquals(1L, result.getId());
        verify(jdbcTemplate, times(1))
                .update(contains("UPDATE todos SET"), any(), any(), any(), any(), any());
    }

}