package com.intern.hub.pm.service;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.pm.dto.task.TaskFilterRequest;
import com.intern.hub.pm.dto.task.TaskResponse;
import com.intern.hub.pm.dto.task.TaskReviewRequest;
import com.intern.hub.pm.dto.task.TaskUpsertRequest;
import com.intern.hub.pm.dto.task.TaskStatisticsResponse;
import com.intern.hub.pm.model.constant.StatusWork;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {

    TaskResponse createTask(Long projectId, TaskUpsertRequest request, List<MultipartFile> files);

    PaginatedData<TaskResponse> getProjectTeamTasks(
            Long teamId, TaskFilterRequest filter,
            int page, int size);

    TaskResponse getTask(Long taskId);

    TaskResponse updateTask(Long taskId, TaskUpsertRequest request, List<MultipartFile> files);

    void deleteTask(Long taskId);

    TaskResponse submitTask(Long taskId, String deliverableDescription, String deliverableLink, List<MultipartFile> files);

    TaskResponse approveTask(Long taskId, TaskReviewRequest request);

    TaskResponse refuseTask(Long taskId, TaskReviewRequest request);

    PaginatedData<TaskResponse> getMyTasks(int page, int size);

    TaskStatisticsResponse getTaskStatistics(Long teamId);
}
