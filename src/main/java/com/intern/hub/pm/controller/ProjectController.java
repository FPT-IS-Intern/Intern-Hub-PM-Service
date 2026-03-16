package com.intern.hub.pm.controller;

import com.intern.hub.pm.dto.project.ProjectResponse;
import com.intern.hub.pm.dto.project.ProjectCompleteRequest;
import com.intern.hub.pm.dto.project.ProjectExtendRequest;
import com.intern.hub.pm.dto.project.ProjectUpsertRequest;
import com.intern.hub.pm.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix:/pm}/projects")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public List<ProjectResponse> getProjects() {
        return projectService.getProjects();
    }

    @GetMapping("/{projectId}")
    public ProjectResponse getProject(@PathVariable Long projectId) {
        return projectService.getProject(projectId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@Valid @RequestPart("request") ProjectUpsertRequest request,
                                         @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return projectService.createProject(request, files);
    }

    @PutMapping(value = "/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProjectResponse updateProject(@PathVariable Long projectId,
                                         @Valid @RequestPart("request") ProjectUpsertRequest request,
                                         @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return projectService.updateProject(projectId, request, files);
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
    }

    @PostMapping("/{projectId}/extend")
    public ProjectResponse extendProject(@PathVariable Long projectId,
                                         @Valid @RequestBody ProjectExtendRequest request) {
        return projectService.extendProject(projectId, request);
    }

    @PostMapping("/{projectId}/complete")
    public ProjectResponse completeProject(@PathVariable Long projectId,
                                           @Valid @RequestBody ProjectCompleteRequest request) {
        return projectService.completeProject(projectId, request);
    }
}
