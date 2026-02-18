package com.gilberto.task_manager_api.repository;

import com.gilberto.task_manager_api.model.Task;
import com.gilberto.task_manager_api.model.enums.TaskPriority;
import com.gilberto.task_manager_api.model.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    Optional<Task> findByIdAndUserId(UUID id, UUID userId);

    Page<Task> findAllByUserId(UUID userId, Pageable pageable);

    Page<Task> findAllByUserIdAndStatus(UUID userId, TaskStatus status, Pageable pageable);

    Page<Task> findAllByUserIdAndPrioridade(UUID userId, TaskPriority prioridade, Pageable pageable);

    Page<Task> findAllByUserIdAndStatusAndPrioridade(UUID userId, TaskStatus status, TaskPriority prioridade, Pageable pageable);
}

