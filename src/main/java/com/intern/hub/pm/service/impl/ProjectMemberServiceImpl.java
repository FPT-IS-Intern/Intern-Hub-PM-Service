package com.intern.hub.pm.service.impl;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.pm.dto.project.member.ProjectMemberCreateRequest;
import com.intern.hub.pm.dto.project.member.ProjectMemberResponse;
import com.intern.hub.pm.dto.project.member.ProjectMemberUpdateRequest;
import com.intern.hub.pm.feign.model.HrmUserClientModel;
import com.intern.hub.pm.model.constant.Status;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.project.Project;
import com.intern.hub.pm.model.project.ProjectMember;
import com.intern.hub.pm.repository.ProjectMemberRepository;
import com.intern.hub.pm.repository.ProjectRepository;
import com.intern.hub.pm.service.ProjectMemberService;
import com.intern.hub.pm.utils.UserContext;
import com.intern.hub.pm.feign.HrmInternalFeignClient;
import com.intern.hub.pm.feign.model.HrmFilterRequest;
import com.intern.hub.pm.feign.model.HrmFilterResponse;
import com.intern.hub.library.common.dto.ResponseApi;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.intern.hub.library.common.dto.ResponseApi.*;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final HrmInternalFeignClient hrmInternalFeignClient;

    @Override
    @Transactional
    public ProjectMemberResponse addMember(Long projectId, ProjectMemberCreateRequest request) {
        Project project = getOwnedActiveProject(projectId);
        if (projectMemberRepository.existsByProjectIdAndUserIdAndStatus(projectId, request.userId(), Status.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User đã là thành viên của dự án");
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
    public PaginatedData<ProjectMemberResponse> getMembers(Long projectId, int page, int size) {
        getActiveProject(projectId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<ProjectMember> memberPage = projectMemberRepository.findAllByProjectIdAndStatus(projectId, Status.ACTIVE, pageable);

        List<ProjectMemberResponse> items = memberPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PaginatedData.<ProjectMemberResponse>builder()
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user này trong dự án"));
    }

    private Project getOwnedActiveProject(Long projectId) {
        Project project = getActiveProject(projectId);
        assertProjectOwner(project);
        return project;
    }

    private Project getActiveProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy dự án"));
        if (project.getStatus() == StatusWork.CANCELED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm tháy dự án");
        }
        return project;
    }

    private void assertProjectOwner(Project project) {
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(project.getCreatorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không phải là chủ dự án này!");
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

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<PaginatedData<HrmFilterResponse>> searchProjectMembers(Long projectId, HrmFilterRequest request, int page, int size) {
        // 1. Fetch exactly the IDs belonging to the project.
        List<Long> projectUserIds = projectMemberRepository.findUserIdsByProjectIdAndStatus(projectId, Status.ACTIVE);
        
        if (projectUserIds.isEmpty()) {
            return ResponseApi.ok(
                    PaginatedData.<HrmFilterResponse>builder()
                            .items(java.util.Collections.emptyList())
                            .totalItems(0L)
                            .totalPages(0)
                            .build()
            );
        }

        // 2. Fetch full user models by IDs from HRM
        ResponseApi<List<HrmUserClientModel>> hrmResponse = hrmInternalFeignClient.getUsersByIdsInternal(projectUserIds);
        
        if (hrmResponse.data() == null || hrmResponse.data().isEmpty()) {
             return ResponseApi.ok(
                    PaginatedData.<HrmFilterResponse>builder()
                            .items(java.util.Collections.emptyList())
                            .totalItems(0L)
                            .totalPages(0)
                            .build()
            );
        }

        // 3. Filter the returned list by keyword locally
        String keyword = request.getKeyword() != null ? request.getKeyword().toLowerCase().trim() : "";
        List<HrmFilterResponse> filteredUsers = hrmResponse.data().stream()
                .filter(u -> keyword.isEmpty() 
                             || (u.fullName() != null && u.fullName().toLowerCase().contains(keyword))
                             || (u.email() != null && u.email().toLowerCase().contains(keyword)))
                .map(u -> HrmFilterResponse.builder()
                        .userId(u.userId())
                        .fullName(u.fullName())
                        .email(u.email())
                        .avatarUrl(u.avatarUrl())
                        .role(u.roleId()) 
                        .build())
                .toList();

        // 4. Implement manual pagination 
        int start = page * size;
        int end = Math.min(start + size, filteredUsers.size());
        List<HrmFilterResponse> pagedUsers = start <= end && start <= filteredUsers.size() 
                ? filteredUsers.subList(start, end) 
                : java.util.Collections.emptyList();

        int totalPages = (int) Math.ceil((double) filteredUsers.size() / size);

        return ResponseApi.ok(
                PaginatedData.<HrmFilterResponse>builder()
                        .items(pagedUsers)
                        .totalItems((long) filteredUsers.size())
                        .totalPages(totalPages)
                        .build()
        );
    }
}
