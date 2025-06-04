package com.emobile.springtodo.service;

import com.emobile.springtodo.dto.request.TodoRequest;
import com.emobile.springtodo.dto.response.TodoResponse;
import com.emobile.springtodo.exception.ResourceNotFoundException;
import com.emobile.springtodo.mapper.TodoMapper;
import com.emobile.springtodo.model.Todo;
import com.emobile.springtodo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;

    @Cacheable(value = "todos", key = "#limit + '-' + #offset")
    public List<TodoResponse> findAll(int limit, int offset) {
        int pageNumber = offset / limit;
        List<Todo> todos = todoRepository.findAll(PageRequest.of(pageNumber, limit)).getContent();
        return todos.stream()
                .map(todoMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "todo", key = "#id")
    public TodoResponse findById(Long id) {
        return todoRepository.findById(id)
                .map(todoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));
    }

    @CacheEvict(value = {"todos", "todo"}, allEntries = true)
    public TodoResponse create(TodoRequest request) {
        Todo todo = todoMapper.toEntity(request);
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());
        Todo savedTodo = todoRepository.save(todo);
        return todoMapper.toResponse(savedTodo);
    }

    @CacheEvict(value = {"todos", "todo"}, allEntries = true)
    public TodoResponse update(Long id, TodoRequest request) {
        Todo existingTodo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));

        existingTodo.setTitle(request.title());
        existingTodo.setDescription(request.description());
        existingTodo.setCompleted(request.completed());
        existingTodo.setUpdatedAt(LocalDateTime.now());

        Todo updatedTodo = todoRepository.save(existingTodo);
        return todoMapper.toResponse(updatedTodo);
    }

    @CacheEvict(value = {"todos", "todo"}, allEntries = true)
    public void delete(Long id) {
        if (!todoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Todo not found with id: " + id);
        }
        todoRepository.deleteById(id);
    }
}