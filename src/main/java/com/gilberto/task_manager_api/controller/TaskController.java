package com.gilberto.task_manager_api.controller;

import com.gilberto.task_manager_api.dto.task.TaskFilter;
import com.gilberto.task_manager_api.dto.task.TaskRequest;
import com.gilberto.task_manager_api.dto.task.TaskResponse;
import com.gilberto.task_manager_api.model.enums.TaskPriority;
import com.gilberto.task_manager_api.model.enums.TaskStatus;
import com.gilberto.task_manager_api.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> listTasks(@RequestParam(required = false) TaskStatus status,
                                                        @RequestParam(required = false) TaskPriority prioridade,
                                                        Pageable pageable,
                                                        Authentication authentication) {
        TaskFilter filter = TaskFilter.builder()
                .status(status)
                .prioridade(prioridade)
                .build();
        Page<TaskResponse> tasks = taskService.listTasks(authentication.getName(), filter, pageable);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request,
                                                   Authentication authentication) {
        TaskResponse response = taskService.createTask(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable("id") UUID id,
                                                   @Valid @RequestBody TaskRequest request,
                                                   Authentication authentication) {
        TaskResponse response = taskService.updateTask(authentication.getName(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") UUID id,
                                           Authentication authentication) {
        taskService.deleteTask(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}

