package com.emobile.springtodo.dto.response;



import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "Response with Task Info")
public record TodoResponse(
        @Schema(description = "Task ID", example = "1")
        @JsonProperty("id")
        Long id,

        @Schema(description = "Task title", example = "Решить контест")
        @JsonProperty("title")
        String title,

        @Schema(description = "Task description", example = "Решить контест до 22 числа")
        @JsonProperty("description")
        String description,

        @Schema(description = "Task completed flag", example = "true/false")
        @JsonProperty("completed")
        boolean completed,

        @Schema(description = "Creating timestamp", example = "2025-04-25T21:25:38.446425700")
        @JsonProperty("createdAt")
        String createdAt,

        @Schema(description = "Updating timestamp", example = "2025-04-25T21:25:38.446425700")
        @JsonProperty("updatedAt")
        String updatedAt
) implements Serializable {}