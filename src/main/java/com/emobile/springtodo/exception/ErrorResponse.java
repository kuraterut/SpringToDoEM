package com.emobile.springtodo.exception;

import java.util.List;

public record ErrorResponse(int status, String message, List<String> errors) {
    ErrorResponse(int status, String message) {
        this(status, message, null);
    }
}