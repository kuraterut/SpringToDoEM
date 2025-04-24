package com.emobile.springtodo.mapper;

import com.emobile.springtodo.dto.request.TodoRequest;
import com.emobile.springtodo.dto.response.TodoResponse;
import com.emobile.springtodo.model.Todo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TodoMapper {
    @Mapping(target = "createdAt", expression = "java(todo.getCreatedAt().toString())")
    @Mapping(target = "updatedAt", expression = "java(todo.getUpdatedAt().toString())")
    TodoResponse toResponse(Todo todo);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Todo toEntity(TodoRequest request);
}
