package com.intern.hub.pm.services;

import com.intern.hub.pm.dtos.request.*;
import com.intern.hub.pm.dtos.response.TaskDetailResponse;
import com.intern.hub.pm.dtos.response.WorkItemDetailResponse;
import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.models.WorkItem;

import java.util.List;

public interface IWorkItemService {

    void createProject(WorkItemRequest request, String emailUser);

    void createModule(Long projectId, WorkItemRequest request, String emailUser);

    void createTask(Long moduleId, TaskRequest request, String emailUser);

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
