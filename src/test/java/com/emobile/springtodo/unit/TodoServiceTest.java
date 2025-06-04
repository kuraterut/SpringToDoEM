package com.emobile.springtodo.unit;


import com.emobile.springtodo.dto.request.TodoRequest;
import com.emobile.springtodo.dto.response.TodoResponse;
import com.emobile.springtodo.mapper.TodoMapper;
import com.emobile.springtodo.model.Todo;
import com.emobile.springtodo.repository.TodoRepository;
import com.emobile.springtodo.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private TodoMapper todoMapper;

    @InjectMocks
    private TodoService todoService;

    private Todo testTodo;
    private TodoResponse testResponse;
    private TodoRequest testRequest;

    @BeforeEach
    void setUp() {
        testTodo = new Todo();
        testTodo.setId(1L);
        testTodo.setTitle("Test Todo");

        testResponse = new TodoResponse(1L, "Test Todo", null, false, null, null);
        testRequest = new TodoRequest("Test Todo", null, false);
    }

    @Test
    @DisplayName("findAll - Should return paginated todos")
    void findAll_ShouldReturnPaginatedTodos() {

        when(todoRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(testTodo)));
        when(todoMapper.toResponse(any(Todo.class))).thenReturn(testResponse);

        var result = todoService.findAll(10, 0);

        assertEquals(1, result.size());
        assertEquals("Test Todo", result.get(0).title());
        verify(todoRepository, times(1)).findAll(PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("findById - Should return todo when exists")
    void findById_ShouldReturnTodoWhenExists() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoMapper.toResponse(testTodo)).thenReturn(testResponse);

        var result = todoService.findById(1L);

        assertEquals("Test Todo", result.title());
        verify(todoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("create - Should save and return new todo")
    void create_ShouldSaveAndReturnNewTodo() {
        when(todoMapper.toEntity(testRequest)).thenReturn(testTodo);
        when(todoRepository.save(testTodo)).thenReturn(testTodo);
        when(todoMapper.toResponse(testTodo)).thenReturn(testResponse);

        var result = todoService.create(testRequest);

        assertEquals("Test Todo", result.title());
        verify(todoRepository, times(1)).save(testTodo);
    }

    @Test
    @DisplayName("update - Should update and return existing todo")
    void update_ShouldSaveAndReturnExistingTodo() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(testTodo)).thenReturn(testTodo);
        when(todoMapper.toResponse(testTodo)).thenReturn(testResponse);

        var result = todoService.update(1L, testRequest);

        assertEquals("Test Todo", result.title());
        verify(todoRepository, times(1)).save(testTodo);
    }

    @Test
    @DisplayName("delete - should delete existing todo")
    void delete_ShouldDeleteWhenTodoExists() {
        Long todoId = 1L;
        when(todoRepository.existsById(todoId)).thenReturn(true);
        doNothing().when(todoRepository).deleteById(todoId);

        todoService.delete(todoId);

        verify(todoRepository, times(1)).existsById(todoId);
        verify(todoRepository, times(1)).deleteById(todoId);
    }
}