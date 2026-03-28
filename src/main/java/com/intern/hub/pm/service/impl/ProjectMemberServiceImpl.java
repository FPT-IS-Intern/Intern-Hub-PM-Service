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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.intern.hub.library.common.dto.ResponseApi.*;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final HrmInternalFeignClient hrmInternalFeignClient;

    @Override
    @Transactional
    public List<ProjectMemberResponse> addMembers(Long projectId, List<ProjectMemberCreateRequest> requests) {
        Project project = getOwnedActiveProject(projectId);

        List<ProjectMember> members = requests.stream().map(request -> {
            if (projectMemberRepository.existsByProjectIdAndUserIdAndStatus(projectId, request.userId(),
                    Status.ACTIVE)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "User ID " + request.userId() + " đã là thành viên của dự án");
            }

            return ProjectMember.builder()
                    .project(project)
                    .userId(request.userId())
                    .role(request.role().trim())
                    .status(Status.ACTIVE)
                    .build();
        }).toList();

        List<ProjectMember> savedMembers = projectMemberRepository.saveAll(members);
        List<Long> userIds = savedMembers.stream().map(ProjectMember::getUserId).toList();
        Map<Long, HrmUserClientModel> userDetailMap = getUserDetailMap(userIds);
        Map<Long, Long> projectCountByUserId = getProjectCountByUserIds(userIds);

        return savedMembers.stream()
                .map(member -> toResponse(
                        member,
                        projectCountByUserId.getOrDefault(member.getUserId(), 0L),
                        userDetailMap.get(member.getUserId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedData<ProjectMemberResponse> getMembers(Long projectId, int page, int size) {
        getActiveProject(projectId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<ProjectMember> memberPage = projectMemberRepository.findAllByProjectIdAndStatus(projectId,
                Status.ACTIVE,
                pageable);
        List<Long> userIds = memberPage.getContent().stream()
                .map(ProjectMember::getUserId)
                .distinct()
                .toList();

        Map<Long, Long> projectCountByUserId = getProjectCountByUserIds(userIds);
        Map<Long, HrmUserClientModel> userDetailMap = getUserDetailMap(userIds);

        List<ProjectMemberResponse> items = memberPage.getContent().stream()
                .map(member -> toResponse(
                        member,
                        projectCountByUserId.getOrDefault(member.getUserId(), 0L),
                        userDetailMap.get(member.getUserId())))
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
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Không tìm thấy user này trong dự án"));
    }

    private Project getOwnedActiveProject(Long projectId) {
        Project project = getActiveProject(projectId);
        assertProjectOwner(project);
        return project;
    }

    private Project getActiveProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy dự án"));
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
        Long countProjectTeam = projectMemberRepository.countActiveProjectsByUserId(
                member.getUserId(),
                Status.ACTIVE,
                StatusWork.CANCELED);
        HrmUserClientModel userDetail = hrmInternalFeignClient.getUserByIdInternal(member.getUserId()).data();
        return toResponse(member, countProjectTeam, userDetail);
    }

    private ProjectMemberResponse toResponse(ProjectMember member, Long countProjectTeam,
                                             HrmUserClientModel userDetail) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getProject().getId(),
                member.getUserId(),
                userDetail != null ? userDetail.fullName() : null,
                userDetail != null ? userDetail.email() : null,
                countProjectTeam,
                member.getRole(),
                member.getStatus(),
                member.getCreatedAt(),
                member.getUpdatedAt());
    }

    private Map<Long, HrmUserClientModel> getUserDetailMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        ResponseApi<List<HrmUserClientModel>> hrmResponse = hrmInternalFeignClient
                .getUsersByIdsInternal(userIds);
        if (hrmResponse.data() == null) {
            return Collections.emptyMap();
        }
        return hrmResponse.data().stream()
                .collect(Collectors.toMap(u -> Long.valueOf(u.userId()), user -> user));
    }

    private Map<Long, Long> getProjectCountByUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return projectMemberRepository.countActiveProjectsByUserIds(userIds, Status.ACTIVE, StatusWork.CANCELED)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<PaginatedData<HrmFilterResponse>> searchProjectMembers(Long projectId,
                                                                              HrmFilterRequest request,
                                                                              int page, int size) {
        // 1. Fetch ProjectMember entities to get project-specific roles
        List<ProjectMember> projectMembers = projectMemberRepository
                .findAllByProjectIdAndStatusOrderByCreatedAtAsc(projectId, Status.ACTIVE);

        if (projectMembers.isEmpty()) {
            return ResponseApi.ok(
                    PaginatedData.<HrmFilterResponse>builder()
                            .items(java.util.Collections.emptyList())
                            .totalItems(0L)
                            .totalPages(0)
                            .build());
        }

        // Build a map of userId -> project role
        Map<Long, String> userRoleMap = projectMembers.stream()
                .collect(Collectors.toMap(ProjectMember::getUserId, ProjectMember::getRole,
                        (existing, replacement) -> existing));

        List<Long> projectUserIds = projectMembers.stream()
                .map(ProjectMember::getUserId).toList();

        // 2. Fetch full user models by IDs from HRM
        ResponseApi<List<HrmUserClientModel>> hrmResponse = hrmInternalFeignClient
                .getUsersByIdsInternal(projectUserIds);

        if (hrmResponse.data() == null || hrmResponse.data().isEmpty()) {
            return ResponseApi.ok(
                    PaginatedData.<HrmFilterResponse>builder()
                            .items(java.util.Collections.emptyList())
                            .totalItems(0L)
                            .totalPages(0)
                            .build());
        }

        String keyword = request.getKeyword() != null ? request.getKeyword().toLowerCase().trim() : "";
        List<HrmFilterResponse> filteredUsers = hrmResponse.data().stream()
                .filter(u -> keyword.isEmpty()
                        || (u.fullName() != null
                        && u.fullName().toLowerCase().contains(keyword))
                        || (u.email() != null && u.email().toLowerCase().contains(keyword)))
                .map(u -> HrmFilterResponse.builder()
                        .userId(u.userId())
                        .fullName(u.fullName())
                        .email(u.email())
                        .avatarUrl(u.avatarUrl())
                        .role(u.userId() != null ? userRoleMap.get(Long.valueOf(u.userId()))
                                : null)
                        .build())
                .toList();

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
                        .build());
    }
}
