package com.intern.hub.pm.service;

import com.intern.hub.pm.dto.task.TaskResponse;
import com.intern.hub.pm.dto.task.TaskReviewRequest;
import com.intern.hub.pm.dto.task.TaskUpsertRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TaskService {

    TaskResponse createTask(Long projectId, TaskUpsertRequest request, List<MultipartFile> files);

    List<TaskResponse> getProjectTasks(Long projectId);

    TaskResponse getTask(Long taskId);

    TaskResponse updateTask(Long taskId, TaskUpsertRequest request, List<MultipartFile> files);

    void deleteTask(Long taskId);

    TaskResponse submitTask(Long taskId, String deliverableLink, List<MultipartFile> files);

    TaskResponse approveTask(Long taskId, TaskReviewRequest request);

    TaskResponse refuseTask(Long taskId, TaskReviewRequest request);

    List<TaskResponse> getMyTasks();
}
