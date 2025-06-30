package com.emobile.springtodo.controller;

import com.emobile.springtodo.dto.request.TodoRequest;
import com.emobile.springtodo.dto.response.TodoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


public interface TodoController {

    @Operation(summary = "Get all TODOs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of TODOs"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    List<TodoResponse> getAllTodos(
            @Parameter(description = "Limit number of items") @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") int offset);

    @Operation(summary = "Get TODO by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the TODO"),
            @ApiResponse(responseCode = "404", description = "TODO not found")
    })
    TodoResponse getTodoById(@Parameter(description = "ID of TODO") @PathVariable Long id);

    @Operation(summary = "Create a new TODO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "TODO created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    TodoResponse createTodo(@Valid @RequestBody TodoRequest request);

    @Operation(summary = "Update TODO by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "TODO updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "TODO not found")
    })
    TodoResponse updateTodo(
            @Parameter(description = "ID of TODO") @PathVariable Long id,
            @Valid @RequestBody TodoRequest request);

    @Operation(summary = "Delete TODO by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "TODO deleted"),
            @ApiResponse(responseCode = "404", description = "TODO not found")
    })
    void deleteTodo(@Parameter(description = "ID of TODO") @PathVariable Long id);
}