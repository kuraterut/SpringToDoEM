package com.emobile.springtodo.dto.response;



import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public record TodoResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("completed") boolean completed,
        @JsonProperty("createdAt") String createdAt,
        @JsonProperty("updatedAt") String updatedAt
) implements Serializable {}