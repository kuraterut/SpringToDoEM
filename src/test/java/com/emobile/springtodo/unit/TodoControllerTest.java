package com.emobile.springtodo.unit;

import com.emobile.springtodo.controller.TodoController;
import com.emobile.springtodo.controller.TodoControllerImpl;
import com.emobile.springtodo.dto.response.TodoResponse;
import com.emobile.springtodo.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TodoControllerTest {

    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoControllerImpl todoControllerImpl;

    private TodoResponse testTodo;

    @BeforeEach
    void setUp() {
        testTodo = new TodoResponse(1L, "Test Todo", "Test Description",
                false, null, null);
    }

    @Test
    @DisplayName("getAllTodos - Should return list of todos")
    void getAllTodos_ShouldReturnTodoList() {
        when(todoService.findAll(10, 0)).thenReturn(List.of(testTodo));

        var response = todoControllerImpl.getAllTodos(10, 0);

        assertEquals(1, response.size());
        assertEquals("Test Todo", response.get(0).title());
        verify(todoService, times(1)).findAll(10, 0);
    }

    @Test
    @DisplayName("getTodoById - Should return todo when exists")
    void getTodoById_ShouldReturnTodoWhenExists() {
        when(todoService.findById(1L)).thenReturn(testTodo);

        var response = todoControllerImpl.getTodoById(1L);

        assertEquals("Test Todo", response.title());
        verify(todoService, times(1)).findById(1L);
    }
}