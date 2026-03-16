package com.intern.hub.pm.controller;

import com.intern.hub.pm.dto.task.TaskResponse;
import com.intern.hub.pm.dto.task.TaskReviewRequest;
import com.intern.hub.pm.dto.task.TaskUpsertRequest;
import com.intern.hub.pm.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix:/pm}")
public class TaskController {

    private final TaskService taskService;

    @PostMapping(value = "/projects/{projectId}/tasks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@PathVariable Long projectId,
                                   @Valid @RequestPart("request") TaskUpsertRequest request,
                                   @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return taskService.createTask(projectId, request, files);
    }

    @GetMapping("/projects/{projectId}/tasks")
    public List<TaskResponse> getProjectTasks(@PathVariable Long projectId) {
        return taskService.getProjectTasks(projectId);
    }

    @GetMapping("/tasks/{taskId}")
    public TaskResponse getTask(@PathVariable Long taskId) {
        return taskService.getTask(taskId);
    }

    @PutMapping(value = "/tasks/{taskId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TaskResponse updateTask(@PathVariable Long taskId,
                                   @Valid @RequestPart("request") TaskUpsertRequest request,
                                   @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return taskService.updateTask(taskId, request, files);
    }

    @DeleteMapping("/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
    }

    @PostMapping(value = "/tasks/{taskId}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TaskResponse submitTask(@PathVariable Long taskId,
                                   @RequestParam(value = "deliverableLink", required = false) String deliverableLink,
                                   @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        return taskService.submitTask(taskId, deliverableLink, files);
    }

    @PostMapping("/tasks/{taskId}/approve")
    public TaskResponse approveTask(@PathVariable Long taskId,
                                    @Valid @RequestBody TaskReviewRequest request) {
        return taskService.approveTask(taskId, request);
    }

    @PostMapping("/tasks/{taskId}/refuse")
    public TaskResponse refuseTask(@PathVariable Long taskId,
                                   @Valid @RequestBody TaskReviewRequest request) {
        return taskService.refuseTask(taskId, request);
    }

    @GetMapping("/my-tasks")
    public List<TaskResponse> getMyTasks() {
        return taskService.getMyTasks();
    }
}
