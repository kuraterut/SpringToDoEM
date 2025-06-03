package com.emobile.springtodo.unit;

import com.emobile.springtodo.model.Todo;
import com.emobile.springtodo.repository.TodoRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TodoRepositoryTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Transaction transaction;

    @Mock
    private Query<Todo> query;

    @InjectMocks
    private TodoRepository todoRepository;

    private Todo testTodo;

    @BeforeEach
    void setUp() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);

        testTodo = Todo.builder()
                .id(1L)
                .title("Test Todo")
                .build();
    }

    @Test
    @DisplayName("findAll - Should execute correct HQL query")
    void findAll_ShouldExecuteCorrectQuery() {
        when(session.createQuery(anyString(), eq(Todo.class))).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.list()).thenReturn(List.of(testTodo));

        List<Todo> result = todoRepository.findAll(10, 0);

        assertEquals(1, result.size());
        verify(session).createQuery("FROM Todo ORDER BY createdAt DESC", Todo.class);
        verify(query).setFirstResult(0);
        verify(query).setMaxResults(10);
    }

    @Test
    @DisplayName("findById - Should return empty when not found")
    void findById_ShouldReturnEmptyWhenNotFound() {
        when(session.get(Todo.class, 1L)).thenReturn(null);

        Optional<Todo> result = todoRepository.findById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("save - Should insert new todo")
    void save_ShouldInsertNewTodo() {
        testTodo.setId(null);

        Todo result = todoRepository.save(testTodo);

        verify(session).beginTransaction();
        verify(session).persist(testTodo);
        verify(transaction).commit();
    }

    @Test
    @DisplayName("save - Should update todo")
    void save_ShouldUpdateTodo() {
        when(session.merge(testTodo)).thenReturn(testTodo);

        Todo result = todoRepository.save(testTodo);

        assertEquals(1L, result.getId());
        verify(session).beginTransaction();
        verify(session).merge(testTodo);
        verify(transaction).commit();
    }

    @Test
    @DisplayName("existsById - should return true when todo exists")
    void existsById_ShouldReturnTrueWhenExists() {
        when(session.get(Todo.class, 1L)).thenReturn(testTodo);

        boolean result = todoRepository.existsById(1L);

        assertTrue(result);
    }
}