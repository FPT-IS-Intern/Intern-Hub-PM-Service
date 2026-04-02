package com.intern.hub.pm.service.impl;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.library.common.exception.ConflictDataException;
import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.dto.project.ApproveRequest;
import com.intern.hub.pm.dto.team.*;
import com.intern.hub.pm.feign.HrmInternalFeignClient;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.repository.specification.TeamSpecification;
import com.intern.hub.pm.model.document.DocumentScope;
import com.intern.hub.pm.model.document.DocumentType;
import com.intern.hub.pm.model.project.Project;
import com.intern.hub.pm.model.team.Team;
import com.intern.hub.pm.model.team.TeamMember;
import com.intern.hub.pm.repository.ProjectRepository;
import com.intern.hub.pm.repository.TaskRepository;
import com.intern.hub.pm.repository.TeamMemberRepository;
import com.intern.hub.pm.repository.TeamRepository;
import com.intern.hub.pm.service.DocumentService;
import com.intern.hub.pm.service.TeamService;
import com.intern.hub.pm.utils.UserContext;
import lombok.RequiredArgsConstructor;
import com.intern.hub.library.common.exception.ForbiddenException;
import com.intern.hub.library.common.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private static final Sort TEAM_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final TeamRepository teamRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final DocumentService documentService;
    private final HrmInternalFeignClient hrmInternalFeignClient;

    @Override
    @Transactional(readOnly = true)
    public PaginatedData<TeamResponse> getTeams(Long projectId, int page, int size) {
        return getTeams(TeamFilterRequest.builder().projectId(projectId).build(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedData<TeamResponse> getTeams(TeamFilterRequest filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, TEAM_SORT);
        Specification<Team> spec = TeamSpecification.filter(filter);
        Page<Team> teamPage = teamRepository.findAll(spec, pageable);

        List<Team> teams = teamPage.getContent();
        return toPaginatedResponse(teams, teamPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedData<TeamResponse> getMyTeams(Long projectId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, TEAM_SORT);
        Long userId = UserContext.requiredUserId();
        Page<Team> teamPage = teamRepository.findByProjectIdAndMemberUserId(projectId, userId, pageable);

        return toPaginatedResponse(teamPage.getContent(), teamPage);
    }

    private PaginatedData<TeamResponse> toPaginatedResponse(List<Team> teams, Page<Team> teamPage) {
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        teams.forEach(t -> {
            if (t.getAssigneeId() != null) userIds.add(t.getAssigneeId());
            if (t.getCreatorId() != null) userIds.add(t.getCreatorId());
        });

        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            try {
                var response = hrmInternalFeignClient.getUsersByIdsInternal(new java.util.ArrayList<>(userIds));
                if (response != null && response.data() != null) {
                    response.data().forEach(u -> userNameMap.put(Long.valueOf(u.userId()), u.fullName()));
                }
            } catch (Exception e) {
                // Keep default IDs if HRM call fails
            }
        }

        return PaginatedData.<TeamResponse>builder()
                .items(teams.stream()
                        .map(t -> toResponseWithLead(t,
                                userNameMap.getOrDefault(t.getAssigneeId(), "Lead (ID: " + t.getAssigneeId() + ")"),
                                userNameMap.getOrDefault(t.getCreatorId(), "User (ID: " + t.getCreatorId() + ")")))
                        .toList())
                .totalItems(teamPage.getTotalElements())
                .totalPages(teamPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamStatisticsResponse getTeamStatistics(Long projectId) {
        if (projectId != null) {
            return TeamStatisticsResponse.builder()
                    .totalTeams(teamRepository.countByProjectIdAndStatusNot(projectId, StatusWork.CANCELED))
                    .notStartedTeams(teamRepository.countByProjectIdAndStatus(projectId, StatusWork.NOT_STARTED))
                    .inProgressTeams(teamRepository.countByProjectIdAndStatus(projectId, StatusWork.IN_PROGRESS))
                    .completedTeams(teamRepository.countByProjectIdAndStatus(projectId, StatusWork.COMPLETED))
                    .overdueTeams(teamRepository.countByProjectIdAndStatus(projectId, StatusWork.OVERDUE))
                    .build();
        } else {
            return TeamStatisticsResponse.builder()
                    .totalTeams(teamRepository.countByStatusNot(StatusWork.CANCELED))
                    .notStartedTeams(teamRepository.countByStatus(StatusWork.NOT_STARTED))
                    .inProgressTeams(teamRepository.countByStatus(StatusWork.IN_PROGRESS))
                    .completedTeams(teamRepository.countByStatus(StatusWork.COMPLETED))
                    .overdueTeams(teamRepository.countByStatus(StatusWork.OVERDUE))
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getTeam(Long teamId) {
        return toResponse(getActiveTeam(teamId));
    }

    @Override
    @Transactional
    public TeamResponse createTeam(Long userId, TeamUpsertRequest request, List<MultipartFile> files) {
        validateDateRange(request.startDate(), request.endDate());
        Project project = getProject(request.projectId());

        Team team = Team.builder()
                .teamUUID(randomNumberUUI())
                .name(request.name().trim())
                .description(request.description().trim())
                .status(StatusWork.NOT_STARTED)
                .budgetToken(request.budgetToken())
                .rewardToken(request.rewardToken())
                .creatorId(userId)
                .assigneeId(request.assigneeId())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .project(project)
                .build();

        Team savedTeam = teamRepository.save(team);

        // Save team members if present
        if (request.memberList() != null && !request.memberList().isEmpty()) {
            List<TeamMember> teamMembers = request.memberList().stream()
                    .map(m -> TeamMember.builder()
                            .userId(m.userId())
                            .status(com.intern.hub.pm.model.constant.Status.ACTIVE)
                            .team(savedTeam)
                            .build())
                    .toList();
            teamMemberRepository.saveAll(teamMembers);
        }

        documentService.replaceDocuments(
                savedTeam.getId(),
                DocumentScope.TEAM,
                DocumentType.CHARTER,
                userId,
                "pm/teams/" + savedTeam.getId() + "/charter",
                files);

        return toResponse(savedTeam);
    }

    @Override
    @Transactional
    public TeamResponse updateTeam(Long teamId, TeamUpsertRequest request, List<MultipartFile> files) {
        Team team = getActiveTeam(teamId);
        assertTeamOwner(team);
        validateDateRange(request.startDate(), request.endDate());

        if (team.getStatus() != StatusWork.NOT_STARTED) {
            throw new ConflictDataException("Chỉ được sửa khi Team chưa bắt đầu");
        }

        team.setName(request.name().trim());
        team.setDescription(request.description().trim());
        team.setBudgetToken(request.budgetToken());
        team.setRewardToken(request.rewardToken());
        team.setAssigneeId(request.assigneeId());
        team.setStartDate(request.startDate());
        team.setEndDate(request.endDate());
        team.setProject(getProject(request.projectId()));

        Team savedTeam = teamRepository.save(team);

        documentService.replaceDocuments(
                savedTeam.getId(),
                DocumentScope.TEAM,
                DocumentType.CHARTER,
                UserContext.requiredUserId(),
                "pm/teams/" + savedTeam.getId() + "/charter",
                files);

        return toResponse(savedTeam);
    }

    @Override
    @Transactional
    public void deleteTeam(Long teamId) {
        Team team = getActiveTeam(teamId);
        assertTeamOwner(team);

        if (team.getStatus() != StatusWork.NOT_STARTED) {
            throw new ConflictDataException("Chỉ đóng dự án khi dự án chưa bắt đầu");
        }

        team.setStatus(StatusWork.CANCELED);
        teamRepository.save(team);
    }

    @Override
    @Transactional
    public TeamResponse completeTeam(Long teamId, TeamCompleteRequest request, List<MultipartFile> files) {
        Team team = getActiveTeam(teamId);
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(team.getAssigneeId())) {
            throw new ForbiddenException("Chỉ người được giao leader mới có thể nộp đáp án");
        }

        long incompleteTaskCount = taskRepository.countByTeamIdAndStatusNotIn(
                teamId,
                java.util.List.of(StatusWork.COMPLETED, StatusWork.CANCELED, StatusWork.REJECTED));
        if (incompleteTaskCount > 0) {
            throw new ConflictDataException("Vẫn còn task trong team chưa được duyệt hoàn thành");
        }

        team.setStatus(StatusWork.PENDING_REVIEW);
        team.setCompletionComment(trimToNull(request.completionComment()));
        team.setDeliverableDescription(trimToNull(request.deliverableDescription()));
        team.setDeliverableLink(trimToNull(request.deliverableLink()));
        Team savedTeam = teamRepository.save(team);

        documentService.replaceDocuments(
                savedTeam.getId(),
                DocumentScope.TEAM,
                DocumentType.DELIVERABLE,
                currentUserId,
                "pm/teams/" + savedTeam.getId() + "/submission",
                files);

        return toResponse(savedTeam);
    }

    @Override
    @Transactional
    public TeamResponse approveTeam(Long teamId, ApproveRequest request) {
        Team team = getActiveTeam(teamId);
        assertTeamOwner(team);

        if (team.getStatus() != StatusWork.PENDING_REVIEW) {
            throw new ConflictDataException("Không phải trạng thái chờ duyệt, chưa duyệt được");
        }

        team.setStatus(StatusWork.COMPLETED);
        team.setNote(trimToNull(request.note()));
        return toResponse(teamRepository.save(team));
    }

    @Override
    @Transactional
    public TeamResponse refuseTeam(Long teamId, ApproveRequest request) {
        Team team = getActiveTeam(teamId);
        assertTeamOwner(team);

        if (team.getStatus() != StatusWork.PENDING_REVIEW) {
            throw new ConflictDataException("Không phải trạng thái chờ duyệt, không yêu cầu sửa được");
        }

        team.setStatus(StatusWork.NEEDS_REVISION);
        team.setNote(trimToNull(request.note()));
        return toResponse(teamRepository.save(team));
    }

    @Override
    public TeamResponse acceptTeam(Long teamId) {
        Team team = getActiveTeam(teamId);
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(team.getAssigneeId())) {
            throw new ForbiddenException("Chỉ người được giao mới có thể nhận team");
        }
        team.setStatus(StatusWork.IN_PROGRESS);
        return toResponse(teamRepository.save(team));
    }

    private Team getActiveTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy team"));
        if (team.getStatus() == StatusWork.CANCELED) {
            throw new NotFoundException("Không tìm thấy team");
        }
        return team;
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dự án"));
    }

    private void assertTeamOwner(Team team) {
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(team.getCreatorId())) {
            throw new ForbiddenException("Bạn không phải là người người tạo team này!");
        }
    }

    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ConflictDataException("Ngày bắt đầu phải trước ngày kết thúc");
        }
    }

    private TeamResponse toResponse(Team team) {
        String leadName = "Lead (ID: " + team.getAssigneeId() + ")";
        String creatorName = "User (ID: " + team.getCreatorId() + ")";
        try {
            if (team.getAssigneeId() != null) {
                var response = hrmInternalFeignClient.getUserByIdInternal(team.getAssigneeId());
                if (response != null && response.data() != null) {
                    leadName = response.data().fullName();
                }
            }
            if (team.getCreatorId() != null) {
                if (team.getCreatorId().equals(team.getAssigneeId())) {
                    creatorName = leadName;
                } else {
                    var response = hrmInternalFeignClient.getUserByIdInternal(team.getCreatorId());
                    if (response != null && response.data() != null) {
                        creatorName = response.data().fullName();
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return toResponseWithLead(team, leadName, creatorName);
    }

    private TeamResponse toResponseWithLead(Team team, String leadName, String creatorName) {
        List<DocumentResponse> charterDocuments = documentService.getDocuments(
                team.getId(),
                DocumentScope.TEAM,
                DocumentType.CHARTER);

        Integer memberCount = teamMemberRepository.countByTeamIdAndStatus(team.getId(),
                com.intern.hub.pm.model.constant.Status.ACTIVE);

        return new TeamResponse(
                String.valueOf(team.getId()),
                team.getTeamUUID(),
                team.getName(),
                team.getDescription(),
                team.getNote(),
                team.getStatus(),
                team.getBudgetToken(),
                team.getRewardToken(),
                team.getCreatorId() != null ? String.valueOf(team.getCreatorId()) : null,
                team.getAssigneeId() != null ? String.valueOf(team.getAssigneeId()) : null,
                creatorName,
                team.getProject() != null ? String.valueOf(team.getProject().getId()) : null,
                team.getDeliverableDescription(),
                team.getDeliverableLink(),
                team.getCompletionComment(),
                team.getStartDate(),
                team.getEndDate(),
                charterDocuments,
                leadName,
                memberCount != null ? memberCount : 0,
                team.getCreatedAt(),
                team.getUpdatedAt());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String randomNumberUUI() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return datePart + String.format("%06d", randomPart).trim();
    }

    @Override
    public TeamResponse refuseTask(Long teamId) {
        Team team = getActiveTeam(teamId);
        Long currentUserId = UserContext.requiredUserId();
        if (!currentUserId.equals(team.getAssigneeId())) {
            throw new ForbiddenException("Chỉ người được giao leader mới có thể từ chối");
        }
        if (team.getStatus() != StatusWork.NOT_STARTED) {
            throw new IllegalArgumentException("Chỉ có thể từ chối team khi ở trạng thái Chưa bắt đầu");
        }
        team.setStatus(StatusWork.REJECTED);
        return toResponse(teamRepository.save(team));
    }
}
