package com.emobile.springtodo.controller;

import com.emobile.springtodo.dto.request.TodoRequest;
import com.emobile.springtodo.dto.response.TodoResponse;
import com.emobile.springtodo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@Slf4j
public class TodoControllerImpl implements TodoController {

    private final TodoService todoService;

    @Override
    @GetMapping
    public List<TodoResponse> getAllTodos(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        log.info("Controller: Start find All");
        List<TodoResponse> todoResponses = todoService.findAll(limit, offset);
        log.info("Controller: End find All");
        return todoResponses;
    }

    @Override
    @GetMapping("/{id}")
    public TodoResponse getTodoById(@PathVariable Long id) {
        return todoService.findById(id);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TodoResponse createTodo(@Valid @RequestBody TodoRequest request) {
        return todoService.create(request);
    }

    @Override
    @PutMapping("/{id}")
    public TodoResponse updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody TodoRequest request) {
        return todoService.update(id, request);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTodo(@PathVariable Long id) {
        todoService.delete(id);
    }
}