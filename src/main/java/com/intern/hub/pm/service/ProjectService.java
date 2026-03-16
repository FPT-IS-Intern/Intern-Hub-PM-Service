package com.intern.hub.pm.service;

import com.intern.hub.pm.dto.project.ProjectResponse;
import com.intern.hub.pm.dto.project.ProjectCompleteRequest;
import com.intern.hub.pm.dto.project.ProjectExtendRequest;
import com.intern.hub.pm.dto.project.ProjectUpsertRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProjectService {

    List<ProjectResponse> getProjects();

    ProjectResponse getProject(Long projectId);

    ProjectResponse createProject(ProjectUpsertRequest request, List<MultipartFile> files);

    ProjectResponse updateProject(Long projectId, ProjectUpsertRequest request, List<MultipartFile> files);

    void deleteProject(Long projectId);

    ProjectResponse extendProject(Long projectId, ProjectExtendRequest request);

    ProjectResponse completeProject(Long projectId, ProjectCompleteRequest request);
}
