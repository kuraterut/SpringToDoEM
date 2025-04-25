package com.emobile.springtodo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Creating/Updating task request")
public record TodoRequest(
        @Schema(description = "Task title", example = "Купить молоко", requiredMode = REQUIRED)
        @NotBlank @Size(max = 100) String title,

        @Schema(description = "Task description", example = "2 литра")
        @Size(max = 500) String description,

        @Schema(description = "Task completed flag", example = "true/false")
        boolean completed
) {}