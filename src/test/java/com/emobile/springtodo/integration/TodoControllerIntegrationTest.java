package com.emobile.springtodo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TodoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("username")
            .withPassword("password")
            .withInitScript("sql/init-test-db.sql");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:latest")
            .withExposedPorts(6379).withStartupTimeout(Duration.ofMinutes(2))
            .withReuse(true);

    private final String TODO_REQUEST_JSON = """
        {
            "title": "Test Todo",
            "description": "Test Description",
            "completed": false
        }
        """;

    private final String EXPECTED_TODO_RESPONSE = """
        {
            "title": "Test Todo",
            "description": "Test Description",
            "completed": false
        }
        """;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("TRUNCATE TABLE todos RESTART IDENTITY CASCADE");
    }

    @Test
    @DisplayName("GET /api/todos - Return first 10 todos")
    void getAllTodos_ShouldReturnTodoList() throws Exception {
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TODO_REQUEST_JSON))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/api/todos")
                        .param("limit", "10")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String expectedJson = "[" + EXPECTED_TODO_RESPONSE + "]";
        System.out.println(result.getResponse().getContentAsString());
        JSONAssert.assertEquals(expectedJson, result.getResponse().getContentAsString(),
                JSONCompareMode.LENIENT);
    }

    @Test
    @DisplayName("GET /api/todos/{id} - Return todo by id")
    void getTodoById_ShouldReturnTodo() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TODO_REQUEST_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String createdTodoJson = createResult.getResponse().getContentAsString();
        String id = new ObjectMapper().readTree(createdTodoJson).path("id").asText();

        MvcResult result = mockMvc.perform(get("/api/todos/{id}", id))
                .andExpect(status().isOk())
                .andReturn();

        JSONAssert.assertEquals(
                EXPECTED_TODO_RESPONSE,
                result.getResponse().getContentAsString(),
                new CustomComparator(JSONCompareMode.LENIENT,
                        new Customization("createdAt", (o1, o2) -> true),
                        new Customization("updatedAt", (o1, o2) -> true)
                )
        );
    }

    @Test
    @DisplayName("POST /api/todos - Create new todo")
    void createTodo_ShouldReturnCreatedTodo() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TODO_REQUEST_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        JSONAssert.assertEquals(EXPECTED_TODO_RESPONSE,
                result.getResponse().getContentAsString(),
                JSONCompareMode.LENIENT);
    }

    @Test
    @DisplayName("PUT /api/todos/{id} - Update todo")
    void updateTodo_ShouldReturnUpdatedTodo() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TODO_REQUEST_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String id = new ObjectMapper()
                .readTree(createResult.getResponse().getContentAsString())
                .path("id").asText();

        String updatedRequestJson = """
        {
            "title": "Updated Todo",
            "description": "Updated Description",
            "completed": true
        }
        """;

        MvcResult result = mockMvc.perform(put("/api/todos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String expectedUpdatedResponse = """
        {
            "id": %s,
            "title": "Updated Todo",
            "description": "Updated Description",
            "completed": true
        }
        """.formatted(id);

        JSONAssert.assertEquals(expectedUpdatedResponse,
                result.getResponse().getContentAsString(),
                JSONCompareMode.LENIENT);
    }

    @Test
    @DisplayName("DELETE /api/todos/{id} - Delete todo")
    void deleteTodo_ShouldReturnNoContent() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TODO_REQUEST_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String id = new ObjectMapper()
                .readTree(createResult.getResponse().getContentAsString())
                .path("id").asText();

        mockMvc.perform(delete("/api/todos/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/todos/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/todos - Return error 400 for invalid input")
    void createTodo_ShouldReturnBadRequestForInvalidInput() throws Exception {
        String invalidTodoJson = """
        {
            "title": "",
            "description": "Test Description",
            "completed": false
        }
        """;

        MvcResult result = mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidTodoJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String expectedErrorJson = """
        {
            "status": 400,
            "errors": ["title: must not be blank"]
        }
        """;

        JSONAssert.assertEquals(expectedErrorJson,
                result.getResponse().getContentAsString(),
                JSONCompareMode.LENIENT);
    }
}