package com.gilberto.task_manager_api.service;

import com.gilberto.task_manager_api.dto.task.TaskFilter;
import com.gilberto.task_manager_api.dto.task.TaskRequest;
import com.gilberto.task_manager_api.dto.task.TaskResponse;
import com.gilberto.task_manager_api.model.Task;
import com.gilberto.task_manager_api.model.User;
import com.gilberto.task_manager_api.model.enums.TaskPriority;
import com.gilberto.task_manager_api.model.enums.TaskStatus;
import com.gilberto.task_manager_api.repository.TaskRepository;
import com.gilberto.task_manager_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email("john@example.com")
                .senha("hashed")
                .build();
    }

    @Test
    void listTasks_withoutFilters_returnsUserTasks() {
        PageRequest pageable = PageRequest.of(0, 10);
        Task task = sampleTask();
        Page<Task> page = new PageImpl<>(List.of(task), pageable, 1);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findAllByUserId(userId, pageable)).thenReturn(page);

        Page<TaskResponse> result = taskService.listTasks(user.getEmail(), TaskFilter.builder().build(), pageable);

        assertThat(result.getContent()).hasSize(1);
        TaskResponse response = result.getContent().get(0);
        assertThat(response.getId()).isEqualTo(task.getId());
        assertThat(response.getUserId()).isEqualTo(userId);
        verify(taskRepository).findAllByUserId(userId, pageable);
    }

    @Test
    void listTasks_withStatusFilter_filtersByStatus() {
        PageRequest pageable = PageRequest.of(0, 10);
        Task task = sampleTask();
        Page<Task> page = new PageImpl<>(List.of(task), pageable, 1);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findAllByUserIdAndStatus(userId, TaskStatus.TODO, pageable)).thenReturn(page);

        TaskFilter filter = TaskFilter.builder().status(TaskStatus.TODO).build();
        Page<TaskResponse> result = taskService.listTasks(user.getEmail(), filter, pageable);

        assertThat(result).hasSize(1);
        verify(taskRepository).findAllByUserIdAndStatus(userId, TaskStatus.TODO, pageable);
    }

    @Test
    void listTasks_withPriorityFilter_filtersByPriority() {
        PageRequest pageable = PageRequest.of(0, 10);
        Task task = sampleTask();
        Page<Task> page = new PageImpl<>(List.of(task), pageable, 1);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findAllByUserIdAndPrioridade(userId, TaskPriority.HIGH, pageable)).thenReturn(page);

        TaskFilter filter = TaskFilter.builder().prioridade(TaskPriority.HIGH).build();
        Page<TaskResponse> result = taskService.listTasks(user.getEmail(), filter, pageable);

        assertThat(result).hasSize(1);
        verify(taskRepository).findAllByUserIdAndPrioridade(userId, TaskPriority.HIGH, pageable);
    }

    @Test
    void listTasks_withBothFilters_filtersByBoth() {
        PageRequest pageable = PageRequest.of(0, 10);
        Task task = sampleTask();
        Page<Task> page = new PageImpl<>(List.of(task), pageable, 1);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findAllByUserIdAndStatusAndPrioridade(userId, TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, pageable))
                .thenReturn(page);

        TaskFilter filter = TaskFilter.builder()
                .status(TaskStatus.IN_PROGRESS)
                .prioridade(TaskPriority.MEDIUM)
                .build();
        Page<TaskResponse> result = taskService.listTasks(user.getEmail(), filter, pageable);

        assertThat(result).hasSize(1);
        verify(taskRepository).findAllByUserIdAndStatusAndPrioridade(userId, TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, pageable);
    }

    @Test
    void createTask_persistsWithUserAndReturnsResponse() {
        TaskRequest request = TaskRequest.builder()
                .titulo("New Task")
                .descricao("Desc")
                .status(TaskStatus.TODO)
                .prioridade(TaskPriority.HIGH)
                .categoria("Work")
                .dueDate(LocalDate.now())
                .build();

        Task saved = sampleTask();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        TaskResponse response = taskService.createTask(user.getEmail(), request);

        assertThat(response.getId()).isEqualTo(saved.getId());
        assertThat(response.getTitulo()).isEqualTo(saved.getTitulo());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_updatesFieldsAndReturnsResponse() {
        UUID taskId = UUID.randomUUID();
        Task existing = sampleTask();
        existing.setId(taskId);

        TaskRequest request = TaskRequest.builder()
                .titulo("Updated")
                .descricao("Updated desc")
                .status(TaskStatus.DONE)
                .prioridade(TaskPriority.LOW)
                .categoria("Home")
                .dueDate(LocalDate.now().plusDays(2))
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(existing));

        TaskResponse response = taskService.updateTask(user.getEmail(), taskId, request);

        assertThat(response.getTitulo()).isEqualTo("Updated");
        assertThat(response.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(response.getPrioridade()).isEqualTo(TaskPriority.LOW);
        assertThat(response.getCategoria()).isEqualTo("Home");
        verify(taskRepository).findByIdAndUserId(taskId, userId);
    }

    @Test
    void deleteTask_removesTaskWhenOwned() {
        UUID taskId = UUID.randomUUID();
        Task existing = sampleTask();
        existing.setId(taskId);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(existing));

        taskService.deleteTask(user.getEmail(), taskId);

        verify(taskRepository).delete(existing);
    }

    @Test
    void operations_throwWhenUserNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.listTasks(user.getEmail(), null, PageRequest.of(0, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateTask_throwsWhenTaskNotOwned() {
        UUID taskId = UUID.randomUUID();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        TaskRequest request = TaskRequest.builder().titulo("x").build();

        assertThatThrownBy(() -> taskService.updateTask(user.getEmail(), taskId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void deleteTask_throwsWhenTaskNotOwned() {
        UUID taskId = UUID.randomUUID();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(user.getEmail(), taskId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task not found");
    }

    private Task sampleTask() {
        return Task.builder()
                .id(UUID.randomUUID())
                .titulo("Sample")
                .descricao("Desc")
                .status(TaskStatus.TODO)
                .prioridade(TaskPriority.MEDIUM)
                .categoria("Work")
                .dueDate(LocalDate.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
    }
}

