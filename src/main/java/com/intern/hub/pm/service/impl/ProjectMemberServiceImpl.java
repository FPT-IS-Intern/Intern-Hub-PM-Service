package com.intern.hub.pm.service.impl;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.library.common.exception.BadRequestException;
import com.intern.hub.pm.dto.project.member.ProjectMemberCreateRequest;
import com.intern.hub.pm.dto.project.member.ProjectMemberResponse;
import com.intern.hub.pm.dto.project.member.ProjectMemberUpdateRequest;
import com.intern.hub.pm.feign.model.HrmUserClientModel;
import com.intern.hub.pm.model.constant.Status;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.project.Project;
import com.intern.hub.pm.model.project.ProjectMember;
import com.intern.hub.pm.repository.*;
import com.intern.hub.pm.service.ProjectMemberService;
import com.intern.hub.pm.utils.UserContext;
import com.intern.hub.pm.feign.HrmInternalFeignClient;
import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.library.common.exception.ConflictDataException;
import com.intern.hub.library.common.exception.ForbiddenException;
import com.intern.hub.library.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final HrmInternalFeignClient hrmInternalFeignClient;

    @Override
    @Transactional
    public List<ProjectMemberResponse> addMembers(Long projectId, List<ProjectMemberCreateRequest> requests) {
        Project project = getOwnedActiveProject(projectId);

        List<ProjectMember> members = requests.stream().map(request -> {
            if (projectMemberRepository.existsByProjectIdAndUserIdAndStatus(projectId, request.userId(),
                    Status.ACTIVE)) {
                throw new BadRequestException(
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
        Map<Long, Long> teamCountByUserId = getTeamCountByUserIds(userIds, projectId);

        return savedMembers.stream()
                .map(member -> toResponse(
                        member,
                        teamCountByUserId.getOrDefault(member.getUserId(), 0L),
                        userDetailMap.get(member.getUserId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedData<ProjectMemberResponse> getMembers(Long projectId, String keyword, int page, int size) {
        getActiveProject(projectId);
        String kw = keyword != null ? keyword.toLowerCase().trim() : "";

        if (kw.isEmpty()) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
            Page<ProjectMember> memberPage = projectMemberRepository.findAllByProjectIdAndStatus(projectId,
                    Status.ACTIVE,
                    pageable);
            List<Long> userIds = memberPage.getContent().stream()
                    .map(ProjectMember::getUserId)
                    .distinct()
                    .toList();

            Map<Long, Long> teamCountByUserId = getTeamCountByUserIds(userIds, projectId);
            Map<Long, HrmUserClientModel> userDetailMap = getUserDetailMap(userIds);

            List<ProjectMemberResponse> items = memberPage.getContent().stream()
                    .map(member -> toResponse(
                            member,
                            teamCountByUserId.getOrDefault(member.getUserId(), 0L),
                            userDetailMap.get(member.getUserId())))
                    .toList();

            return PaginatedData.<ProjectMemberResponse>builder()
                    .items(items)
                    .totalItems(memberPage.getTotalElements())
                    .totalPages(memberPage.getTotalPages())
                    .build();
        }

        // If keyword is present, fetch all and filter in memory
        List<ProjectMember> allMembers = projectMemberRepository
                .findAllByProjectIdAndStatusOrderByCreatedAtAsc(projectId, Status.ACTIVE);

        if (allMembers.isEmpty()) {
            return PaginatedData.<ProjectMemberResponse>builder()
                    .items(Collections.emptyList())
                    .totalItems(0L)
                    .totalPages(0)
                    .build();
        }

        List<Long> userIds = allMembers.stream().map(ProjectMember::getUserId).toList();
        Map<Long, HrmUserClientModel> userDetailMap = getUserDetailMap(userIds);
        Map<Long, Long> teamCountByUserId = getTeamCountByUserIds(userIds, projectId);

        List<ProjectMemberResponse> filteredItems = allMembers.stream()
                .map(member -> toResponse(
                        member,
                        teamCountByUserId.getOrDefault(member.getUserId(), 0L),
                        userDetailMap.get(member.getUserId())))
                .filter(res -> (res.getFullName() != null
                        && res.getFullName().toLowerCase().contains(kw))
                        || (res.getEmail() != null
                        && res.getEmail().toLowerCase().contains(kw)))
                .collect(Collectors.toList());

        int start = page * size;
        int end = Math.min(start + size, filteredItems.size());
        List<ProjectMemberResponse> pagedItems = start <= end && start <= filteredItems.size()
                ? filteredItems.subList(start, end)
                : Collections.emptyList();

        int totalPages = (int) Math.ceil((double) filteredItems.size() / size);

        return PaginatedData.<ProjectMemberResponse>builder()
                .items(pagedItems)
                .totalItems((long) filteredItems.size())
                .totalPages(totalPages)
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

        Project project = member.getProject();
        Long userId = member.getUserId();
        Long projectId = project.getId();

        if (project.getStatus() != StatusWork.CANCELED) {
            long leaderCount = teamRepository.countByAssigneeIdAndProjectIdAndStatusNot(
                    userId,
                    projectId,
                    StatusWork.CANCELED);
            if (leaderCount > 0) {
                throw new BadRequestException(
                        "Thành viên đang là trưởng nhóm của một nhóm trong dự án, không thể xóa");
            }

            // Kiểm tra nếu thành viên đang tham gia vào bất kỳ nhóm (Team Member) nào trong
            // dự án
            long teamMemberCount = teamMemberRepository.countActiveTeamsByUserId(
                    userId,
                    Status.ACTIVE,
                    StatusWork.CANCELED,
                    projectId);
            if (teamMemberCount > 0) {
                throw new BadRequestException(
                        "Thành viên vẫn đang tham gia vào nhóm trong dự án, vui lòng xóa khỏi nhóm trước khi xóa khỏi dự án");
            }
        }

        member.setStatus(Status.DELETED);
        projectMemberRepository.save(member);
    }

    private ProjectMember getActiveMember(Long memberId) {
        return projectMemberRepository.findByIdAndStatus(memberId, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy user này trong dự án"));
    }

    private Project getOwnedActiveProject(Long projectId) {
        Project project = getActiveProject(projectId);
        assertProjectOwner(project);
        return project;
    }

    private Project getActiveProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dự án"));
        if (project.getStatus() == StatusWork.CANCELED) {
            throw new NotFoundException("Không tìm thấy dự án");
        }
        return project;
    }

    private void assertProjectOwner(Project project) {
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(project.getCreatorId())) {
            throw new BadRequestException("Bạn không phải là chủ dự án này!");
        }
    }

    private ProjectMemberResponse toResponse(ProjectMember member) {
        Long projectId = member.getProject().getId();
        Long countProjectTeam = teamRepository.countByAssigneeIdAndProjectIdAndStatusNot(
                member.getUserId(),
                projectId,
                StatusWork.CANCELED);
        HrmUserClientModel userDetail = hrmInternalFeignClient.getUserByIdInternal(member.getUserId()).data();
        return toResponse(member, countProjectTeam, userDetail);
    }

    private ProjectMemberResponse toResponse(
            ProjectMember member,
            Long countProjectTeam,
            HrmUserClientModel userDetail) {
        return new ProjectMemberResponse(
                String.valueOf(member.getId()),
                String.valueOf(member.getProject().getId()),
                String.valueOf(member.getUserId()),
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

    private Map<Long, Long> getTeamCountByUserIds(List<Long> userIds, Long projectId) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return teamRepository.countTeamsByAssigneeIds(userIds, projectId, StatusWork.CANCELED)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]));
    }

}
