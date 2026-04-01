package com.intern.hub.pm.service;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.pm.dto.project.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProjectService {

    PaginatedData<ProjectResponse> getProjects(int page, int size);

    PaginatedData<ProjectResponse> getProjects(ProjectFilterRequest filter, int page, int size);

    ProjectStatisticsResponse getProjectStatistics();

    ProjectResponse getProject(Long projectId);

    ProjectResponse createProject(Long userId, ProjectUpsertRequest request, List<MultipartFile> files);

    ProjectResponse updateProject(Long projectId, ProjectUpsertRequest request, List<MultipartFile> files);

    void deleteProject(Long projectId);

    ProjectResponse extendProject(Long projectId, ProjectExtendRequest request);

    ProjectResponse completeProject(Long projectId, ProjectCompleteRequest request);

    ProjectResponse acceptProject(Long projectId);
}
