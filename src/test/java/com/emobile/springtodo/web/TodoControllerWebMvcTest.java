package com.emobile.springtodo.web;

import com.emobile.springtodo.controller.TodoControllerImpl;
import com.emobile.springtodo.dto.request.TodoRequest;
import com.emobile.springtodo.dto.response.TodoResponse;
import com.emobile.springtodo.exception.ResourceNotFoundException;
import com.emobile.springtodo.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TodoControllerImpl.class)
class TodoControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoService todoService;

    private final TodoResponse testResponse = new TodoResponse(
            1L, "Test Todo", "Test Description", false, null, null);

    @Test
    @DisplayName("GET /api/todos - Should return 200 with todos list")
    void getAllTodos_ShouldReturnTodosList() throws Exception {
        given(todoService.findAll(anyInt(), anyInt()))
                .willReturn(List.of(testResponse));

        mockMvc.perform(get("/api/todos")
                        .param("limit", "10")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Todo"))
                .andExpect(jsonPath("$[0].completed").value(false));

        verify(todoService).findAll(10, 0);
    }

    @Test
    @DisplayName("GET /api/todos/{id} - Should return 200 when todo exists")
    void getTodoById_ShouldReturnTodoWhenExists() throws Exception {
        given(todoService.findById(anyLong()))
                .willReturn(testResponse);

        mockMvc.perform(get("/api/todos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Todo"));

        verify(todoService).findById(1L);
    }

    @Test
    @DisplayName("POST /api/todos - Should return 201 when todo created")
    void createTodo_ShouldReturnCreatedTodo() throws Exception {
        TodoRequest request = new TodoRequest("New Todo", "New Desc", false);
        given(todoService.create(any(TodoRequest.class)))
                .willReturn(testResponse);

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Todo"));

        verify(todoService).create(any(TodoRequest.class));
    }

    @Test
    @DisplayName("PUT /api/todos/{id} - Should return 200 when todo updated")
    void updateTodo_ShouldReturnUpdatedTodo() throws Exception {
        TodoRequest request = new TodoRequest("Updated Todo", "Updated Desc", true);
        given(todoService.update(anyLong(), any(TodoRequest.class)))
                .willReturn(testResponse);

        mockMvc.perform(put("/api/todos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Todo"));

        verify(todoService).update(1L, request);
    }

    @Test
    @DisplayName("DELETE /api/todos/{id} - Should return 204 when todo deleted")
    void deleteTodo_ShouldReturnNoContent() throws Exception {
        Long todoId = 1L;

        mockMvc.perform(delete("/api/todos/{id}", todoId))
                .andExpect(status().isNoContent());

        verify(todoService).delete(todoId);
    }

    @Test
    @DisplayName("POST /api/todos - Should return 400 when invalid input")
    void createTodo_ShouldReturnBadRequestWhenInvalid() throws Exception {
        TodoRequest invalidRequest = new TodoRequest("", null, false);

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("GET /api/todos/{id} - Should return 404 when todo not found")
    void getTodoById_ShouldReturnNotFound() throws Exception {
        given(todoService.findById(anyLong()))
                .willThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/todos/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not found"));
    }
}