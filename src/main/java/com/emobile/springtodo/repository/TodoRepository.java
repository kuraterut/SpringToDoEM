package com.emobile.springtodo.repository;

import com.emobile.springtodo.model.Todo;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Repository
public class TodoRepository {

    private final JdbcTemplate jdbcTemplate;

    public TodoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Todo> findAll(int limit, int offset) {
        String sql = "SELECT * FROM todos ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new TodoRowMapper(), limit, offset);
    }

    public Optional<Todo> findById(Long id) {
        String sql = "SELECT * FROM todos WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new TodoRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    public Todo save(Todo todo) {
        if (todo.getId() == null) {
            return insert(todo);
        } else {
            return update(todo);
        }
    }

    private Todo insert(Todo todo) {
        String sql = "INSERT INTO todos (title, description, completed, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(sql, Long.class,
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getCreatedAt(),
                todo.getUpdatedAt());
        todo.setId(id);
        return todo;
    }

    private Todo update(Todo todo) {
        String sql = "UPDATE todos SET title = ?, description = ?, completed = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getUpdatedAt(),
                todo.getId());
        return todo;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM todos WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }


    private static class TodoRowMapper implements RowMapper<Todo> {
        @Override
        public Todo mapRow(ResultSet rs, int rowNum) throws SQLException {
            Todo todo = new Todo();
            todo.setId(rs.getLong("id"));
            todo.setTitle(rs.getString("title"));
            todo.setDescription(rs.getString("description"));
            todo.setCompleted(rs.getBoolean("completed"));
            todo.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().truncatedTo(ChronoUnit.SECONDS));
            todo.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime().truncatedTo(ChronoUnit.SECONDS));
            return todo;
        }
    }


}