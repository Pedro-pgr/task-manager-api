package com.gilberto.task_manager_api.service;

import com.gilberto.task_manager_api.dto.task.TaskFilter;
import com.gilberto.task_manager_api.dto.task.TaskRequest;
import com.gilberto.task_manager_api.dto.task.TaskResponse;
import com.gilberto.task_manager_api.exception.ResourceNotFoundException;
import com.gilberto.task_manager_api.model.Task;
import com.gilberto.task_manager_api.model.User;
import com.gilberto.task_manager_api.repository.TaskRepository;
import com.gilberto.task_manager_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class    TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public Page<TaskResponse> listTasks(String userEmail, TaskFilter filter, Pageable pageable) {
        User user = getUserByEmail(userEmail);

        if (filter != null && filter.getStatus() != null && filter.getPrioridade() != null) {
            return taskRepository.findAllByUserIdAndStatusAndPrioridade(user.getId(), filter.getStatus(), filter.getPrioridade(), pageable)
                    .map(this::toResponse);
        }
        if (filter != null && filter.getStatus() != null) {
            return taskRepository.findAllByUserIdAndStatus(user.getId(), filter.getStatus(), pageable)
                    .map(this::toResponse);
        }
        if (filter != null && filter.getPrioridade() != null) {
            return taskRepository.findAllByUserIdAndPrioridade(user.getId(), filter.getPrioridade(), pageable)
                    .map(this::toResponse);
        }
        return taskRepository.findAllByUserId(user.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional
    public TaskResponse createTask(String userEmail, TaskRequest request) {
        User user = getUserByEmail(userEmail);

        Task task = Task.builder()
                .titulo(request.getTitulo())
                .descricao(request.getDescricao())
                .status(request.getStatus())
                .prioridade(request.getPrioridade())
                .categoria(request.getCategoria())
                .dueDate(request.getDueDate())
                .user(user)
                .build();

        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    @Transactional
    public TaskResponse updateTask(String userEmail, UUID taskId, TaskRequest request) {
        User user = getUserByEmail(userEmail);
        Task task = taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        task.setTitulo(request.getTitulo());
        task.setDescricao(request.getDescricao());
        task.setStatus(request.getStatus() != null ? request.getStatus() : task.getStatus());
        task.setPrioridade(request.getPrioridade() != null ? request.getPrioridade() : task.getPrioridade());
        task.setCategoria(request.getCategoria());
        task.setDueDate(request.getDueDate());

        return toResponse(task);
    }

    @Transactional
    public void deleteTask(String userEmail, UUID taskId) {
        User user = getUserByEmail(userEmail);
        Task task = taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        taskRepository.delete(task);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .titulo(task.getTitulo())
                .descricao(task.getDescricao())
                .status(task.getStatus())
                .prioridade(task.getPrioridade())
                .categoria(task.getCategoria())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .userId(task.getUser().getId())
                .build();
    }

    public TaskResponse findById(String userEmail, UUID taskId) {
        User user = getUserByEmail(userEmail);

        return taskRepository.findByIdAndUserId(taskId, user.getId())
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found by this id for the user"));
  }
}

