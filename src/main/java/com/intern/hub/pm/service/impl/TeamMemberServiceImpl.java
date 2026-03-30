package com.intern.hub.pm.service.impl;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.library.common.exception.ConflictDataException;
import com.intern.hub.library.common.exception.NotFoundException;
import com.intern.hub.pm.dto.team.TeamMemberCreateRequest;
import com.intern.hub.pm.dto.team.TeamMemberResponse;
import com.intern.hub.pm.feign.HrmInternalFeignClient;
import com.intern.hub.pm.feign.model.HrmUserClientModel;
import com.intern.hub.pm.model.constant.Status;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.project.ProjectMember;
import com.intern.hub.pm.model.team.Team;
import com.intern.hub.pm.model.team.TeamMember;
import com.intern.hub.pm.repository.ProjectMemberRepository;
import com.intern.hub.pm.repository.TaskRepository;
import com.intern.hub.pm.repository.TeamMemberRepository;
import com.intern.hub.pm.repository.TeamRepository;
import com.intern.hub.pm.service.TeamMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamMemberServiceImpl implements TeamMemberService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final HrmInternalFeignClient hrmInternalFeignClient;

    @Override
    @Transactional
    public List<TeamMemberResponse> addMembers(Long teamId, List<TeamMemberCreateRequest> requests) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy team"));

        // 1. Lấy danh sách thành viên hiện có trong Team để kiểm tra trùng lặp
        Set<Long> existingMemberIds = teamMemberRepository.findAllByTeamId(teamId).stream()
                .filter(m -> m.getStatus() == Status.ACTIVE)
                .map(TeamMember::getUserId)
                .collect(Collectors.toSet());

        // 2. Kiểm tra xem các User này có thuộc dự án (Project) chứa Team này không
        Long projectId = team.getProject().getId();
        
        // Fetch all active project members for this project
        // Note: ProjectMemberRepository.findUserIdsByProjectIdAndStatus is very efficient here
        Set<Long> projectMemberIds = new HashSet<>(
                projectMemberRepository.findUserIdsByProjectIdAndStatus(projectId, Status.ACTIVE)
        );

        List<TeamMember> members = requests.stream().map(request -> {
            Long userId = request.userId();
            
            if (existingMemberIds.contains(userId)) {
                throw new ConflictDataException("User ID " + userId + " đã là thành viên của team");
            }
            
            if (!projectMemberIds.contains(userId)) {
                throw new ConflictDataException("User ID " + userId + " không phải là thành viên của dự án này");
            }

            return TeamMember.builder()
                    .team(team)
                    .userId(userId)
                    .status(Status.ACTIVE)
                    .build();
        }).toList();

        List<TeamMember> savedMembers = teamMemberRepository.saveAll(members);
        List<Long> userIds = savedMembers.stream().map(TeamMember::getUserId).toList();
        Map<Long, HrmUserClientModel> userDetailMap = getUserDetailMap(userIds);

        return savedMembers.stream()
                .map(member -> toResponse(member, userDetailMap.get(member.getUserId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedData<TeamMemberResponse> getMembers(Long teamId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<TeamMember> memberPage = teamMemberRepository.findAllByTeamId(teamId, pageable);
        
        List<Long> userIds = memberPage.getContent().stream()
                .map(TeamMember::getUserId)
                .distinct()
                .toList();

        Map<Long, HrmUserClientModel> userDetailMap = getUserDetailMap(userIds);

        List<TeamMemberResponse> items = memberPage.getContent().stream()
                .map(member -> toResponse(member, userDetailMap.get(member.getUserId())))
                .toList();

        return PaginatedData.<TeamMemberResponse>builder()
                .items(items)
                .totalItems(memberPage.getTotalElements())
                .totalPages(memberPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedData<TeamMemberResponse> searchMembers(Long teamId, String keyword, int page, int size) {
        List<TeamMember> allMembers = teamMemberRepository.findAllByTeamId(teamId);
        if (allMembers.isEmpty()) {
            return PaginatedData.<TeamMemberResponse>builder()
                    .items(Collections.emptyList())
                    .totalItems(0L)
                    .totalPages(0)
                    .build();
        }

        List<Long> userIds = allMembers.stream().map(TeamMember::getUserId).toList();
        Map<Long, HrmUserClientModel> userDetailMap = getUserDetailMap(userIds);

        String kw = keyword != null ? keyword.toLowerCase().trim() : "";
        List<TeamMemberResponse> filteredItems = allMembers.stream()
                .map(member -> toResponse(member, userDetailMap.get(member.getUserId())))
                .filter(res -> kw.isEmpty()
                        || (res.getFullName() != null && res.getFullName().toLowerCase().contains(kw))
                        || (res.getEmail() != null && res.getEmail().toLowerCase().contains(kw)))
                .collect(Collectors.toList());

        int start = page * size;
        int end = Math.min(start + size, filteredItems.size());
        List<TeamMemberResponse> pagedItems = start <= end && start <= filteredItems.size()
                ? filteredItems.subList(start, end)
                : Collections.emptyList();

        int totalPages = (int) Math.ceil((double) filteredItems.size() / size);

        return PaginatedData.<TeamMemberResponse>builder()
                .items(pagedItems)
                .totalItems((long) filteredItems.size())
                .totalPages(totalPages)
                .build();
    }

    @Override
    @Transactional
    public void deleteMember(Long memberId) {
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thành viên team"));
        member.setStatus(Status.DELETED);
        teamMemberRepository.save(member);
    }

    private TeamMemberResponse toResponse(TeamMember member, HrmUserClientModel userDetail) {
        long taskCount = taskRepository.countByTeamIdAndAssigneeIdAndStatusNot(
                member.getTeam().getId(),
                member.getUserId(),
                StatusWork.CANCELED
        );

        String role = projectMemberRepository.findByProjectIdAndUserIdAndStatus(
                        member.getTeam().getProject().getId(),
                        member.getUserId(),
                        Status.ACTIVE)
                .map(com.intern.hub.pm.model.project.ProjectMember::getRole)
                .orElse("DEVELOPER");

        return TeamMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .teamId(member.getTeam().getId())
                .fullName(userDetail != null ? userDetail.fullName() : "User (ID: " + member.getUserId() + ")")
                .email(userDetail != null ? userDetail.email() : null)
                .status(member.getStatus() != null ? member.getStatus().name() : null)
                .role(role)
                .countTasks(taskCount)
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
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
}
