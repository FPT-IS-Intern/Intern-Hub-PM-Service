package com.intern.hub.pm.service;

import com.intern.hub.pm.dto.request.*;
import com.intern.hub.pm.dto.response.TaskDetailResponse;
import com.intern.hub.pm.dto.response.WorkItemDetailResponse;
import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.model.WorkItem;

import java.util.List;

public interface IWorkItemService {

    void createProject(WorkItemRequest request, Long userId);

    void createModule(Long projectId, WorkItemRequest request, Long userId);

    void createTask(Long moduleId, TaskRequest request, Long userId);

    WorkItemDetailResponse workItemDetailResponse(Long id);

    void addUserProject(Long projectId, List<UserProjectRequest> requests);

    void addUserModule(Long moduleId, List<UserProjectRequest> requests);

    void editProject(Long id, WorkItemRequest request);

    void deleteWork(Long id, WorkItemType workType);

    WorkItem findById(Long id);

    WorkItem refuse(Long workId, NoteRequest request, WorkItemType workItemType);

    void editTask(Long id, EditTaskRequest request);

    TaskDetailResponse taskDetail(Long id);

    void submit(Long taskId, SubmitTaskRequest request, WorkItemType workItemType);

    long countTaskByUser(
            WorkItemType workType,
            Long projectId,
            Long userId,
            StatusWork statusWork
    );
}

