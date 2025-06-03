package com.emobile.springtodo.repository;

import com.emobile.springtodo.model.Todo;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.hibernate.*;

@Repository
public class TodoRepository {

    private final SessionFactory sessionFactory;

    public TodoRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<Todo> findAll(int limit, int offset) {
        try (Session session = sessionFactory.openSession()) {
            Query<Todo> query = session.createQuery("FROM Todo ORDER BY createdAt DESC", Todo.class);
            query.setMaxResults(limit);
            query.setFirstResult(offset);
            return query.list();
        }
    }

    public Optional<Todo> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Todo todo = session.get(Todo.class, id);
            return Optional.ofNullable(todo);
        }
    }

    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    public Todo save(Todo todo) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                if (todo.getId() == null) {
                    session.persist(todo);
                } else {
                    session.merge(todo);
                }
                transaction.commit();
                return todo;
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public void deleteById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Todo todo = session.get(Todo.class, id);
                if (todo != null) {
                    session.remove(todo);
                }
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }
}