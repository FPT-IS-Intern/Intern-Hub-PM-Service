package com.intern.hub.pm.controller;

import com.intern.hub.pm.dto.task.TaskResponse;
import com.intern.hub.pm.dto.task.TaskReviewRequest;
import com.intern.hub.pm.dto.task.TaskUpsertRequest;
import com.intern.hub.pm.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Task", description = "Các thao tác quản lý task và workflow nộp bài")
@SecurityRequirement(name = "Bearer")
public class TaskController {

    private final TaskService taskService;

    @PostMapping(value = "/projects/{projectId}/tasks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo task", description = "Tạo mới task trong dự án và có thể đính kèm tài liệu hướng dẫn.")
    public TaskResponse createTask(@PathVariable Long projectId,
                                   @Valid @RequestPart("request") TaskUpsertRequest request,
                                   @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return taskService.createTask(projectId, request, files);
    }

    @GetMapping("/projects/{projectId}/tasks")
    @Operation(summary = "Lấy danh sách task theo dự án", description = "Trả về danh sách task đang hoạt động của dự án.")
    public List<TaskResponse> getProjectTasks(@PathVariable Long projectId) {
        return taskService.getProjectTasks(projectId);
    }

    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "Lấy chi tiết task", description = "Trả về thông tin chi tiết của task theo id.")
    public TaskResponse getTask(@PathVariable Long taskId) {
        return taskService.getTask(taskId);
    }

    @PutMapping(value = "/tasks/{taskId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật task", description = "Cập nhật thông tin task và thay thế bộ tài liệu hướng dẫn nếu có file mới.")
    public TaskResponse updateTask(@PathVariable Long taskId,
                                   @Valid @RequestPart("request") TaskUpsertRequest request,
                                   @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return taskService.updateTask(taskId, request, files);
    }

    @DeleteMapping("/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Hủy task", description = "Hủy mềm task bằng cách chuyển trạng thái sang CANCELED.")
    public void deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
    }

    @PostMapping(value = "/tasks/{taskId}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Nộp bài task", description = "Nộp kết quả task, thay thế bộ file submission cũ bằng bộ mới nhất.")
    public TaskResponse submitTask(@PathVariable Long taskId,
                                   @RequestParam(value = "deliverableLink", required = false) String deliverableLink,
                                   @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        return taskService.submitTask(taskId, deliverableLink, files);
    }

    @PostMapping("/tasks/{taskId}/approve")
    @Operation(summary = "Duyệt task", description = "Duyệt task khi task đang ở trạng thái chờ duyệt.")
    public TaskResponse approveTask(@PathVariable Long taskId,
                                    @Valid @RequestBody TaskReviewRequest request) {
        return taskService.approveTask(taskId, request);
    }

    @PostMapping("/tasks/{taskId}/refuse")
    @Operation(summary = "Yêu cầu làm lại task", description = "Từ chối và yêu cầu chỉnh sửa task khi task đang ở trạng thái chờ duyệt.")
    public TaskResponse refuseTask(@PathVariable Long taskId,
                                   @Valid @RequestBody TaskReviewRequest request) {
        return taskService.refuseTask(taskId, request);
    }

    @GetMapping("/my-tasks")
    @Operation(summary = "Lấy task của tôi", description = "Trả về danh sách task được giao cho người dùng hiện tại.")
    public List<TaskResponse> getMyTasks() {
        return taskService.getMyTasks();
    }
}
