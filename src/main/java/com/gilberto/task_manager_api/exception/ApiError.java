package com.gilberto.task_manager_api.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ApiError {
    private final int status;
    private final String error;
    private final String message;
    private final List<String> errors;
    private final String path;
    private final LocalDateTime timestamp;
}

