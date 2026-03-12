package com.intern.hub.pm.service;

import com.intern.hub.pm.dto.request.*;
import com.intern.hub.pm.dto.response.TaskDetailResponse;
import com.intern.hub.pm.dto.response.WorkItemDetailResponse;
import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.model.WorkItem;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IWorkItemService {

    void createProject(WorkItemRequest request, List<MultipartFile> files, Long userId);

    void createTask(Long projectId, TaskRequest request, List<MultipartFile> files, Long userId);

    WorkItemDetailResponse workItemDetailResponse(Long id);

    void addUserProject(Long projectId, List<UserProjectRequest> requests);

    void editProject(Long id, WorkItemRequest request, List<MultipartFile> files);

    void deleteWork(Long id, WorkItemType workType);

    WorkItem findById(Long id);

    WorkItem refuseTask(Long taskId, NoteRequest request);

    WorkItem approveTask(Long taskId, ApproveTaskRequest request);

    void editTask(Long id, EditTaskRequest request, List<MultipartFile> files);

    TaskDetailResponse taskDetail(Long id);

    void submitTask(Long taskId, SubmitTaskRequest request, List<MultipartFile> files);

    WorkItem extendProject(Long projectId, ExtendProjectRequest request);

    WorkItem completeProject(Long projectId, CompleteProjectRequest request);

    long countTaskByUser(
            WorkItemType workType,
            Long projectId,
            Long userId,
            StatusWork statusWork
    );
}

