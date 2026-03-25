package com.intern.hub.pm.service.impl;

import com.intern.hub.pm.dto.project.member.ProjectMemberCreateRequest;
import com.intern.hub.pm.dto.project.member.ProjectMemberResponse;
import com.intern.hub.pm.dto.project.member.ProjectMemberUpdateRequest;
import com.intern.hub.pm.model.constant.Status;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.project.Project;
import com.intern.hub.pm.model.project.ProjectMember;
import com.intern.hub.pm.repository.ProjectMemberRepository;
import com.intern.hub.pm.repository.ProjectRepository;
import com.intern.hub.pm.service.ProjectMemberService;
import com.intern.hub.pm.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional
    public ProjectMemberResponse addMember(Long projectId, ProjectMemberCreateRequest request) {
        Project project = getOwnedActiveProject(projectId);
        if (projectMemberRepository.existsByProjectIdAndUserIdAndStatus(projectId, request.userId(), Status.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a project member");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .userId(request.userId())
                .role(request.role().trim())
                .status(Status.ACTIVE)
                .build();

        return toResponse(projectMemberRepository.save(member));
    }

    @Override
    @Transactional(readOnly = true)
    public com.intern.hub.library.common.dto.PaginatedData<ProjectMemberResponse> getMembers(Long projectId, int page, int size) {
        getActiveProject(projectId);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "createdAt"));
        org.springframework.data.domain.Page<ProjectMember> memberPage = projectMemberRepository.findAllByProjectIdAndStatus(projectId, Status.ACTIVE, pageable);

        List<ProjectMemberResponse> items = memberPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return com.intern.hub.library.common.dto.PaginatedData.<ProjectMemberResponse>builder()
                .items(items)
                .totalItems(memberPage.getTotalElements())
                .totalPages(memberPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public ProjectMemberResponse updateMember(Long memberId, ProjectMemberUpdateRequest request) {
        ProjectMember member = getActiveMember(memberId);
        assertProjectOwner(member.getProject());
        member.setRole(request.role().trim());
        return toResponse(projectMemberRepository.save(member));
    }

    @Override
    @Transactional
    public void deleteMember(Long memberId) {
        ProjectMember member = getActiveMember(memberId);
        assertProjectOwner(member.getProject());
        member.setStatus(Status.DELETED);
        projectMemberRepository.save(member);
    }

    private ProjectMember getActiveMember(Long memberId) {
        return projectMemberRepository.findByIdAndStatus(memberId, Status.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project member not found"));
    }

    private Project getOwnedActiveProject(Long projectId) {
        Project project = getActiveProject(projectId);
        assertProjectOwner(project);
        return project;
    }

    private Project getActiveProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (project.getStatus() == StatusWork.CANCELED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
        return project;
    }

    private void assertProjectOwner(Project project) {
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(project.getCreatorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project owner can modify project members");
        }
    }

    private ProjectMemberResponse toResponse(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getProject().getId(),
                member.getUserId(),
                member.getRole(),
                member.getStatus(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }
}
