package com.intern.hub.pm.service.impl;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.library.common.exception.ConflictDataException;
import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.dto.project.ApproveRequest;
import com.intern.hub.pm.dto.team.*;
import com.intern.hub.pm.feign.HrmInternalFeignClient;
import com.intern.hub.pm.feign.model.HrmUserClientModel;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.repository.specification.TeamSpecification;
import com.intern.hub.pm.model.document.DocumentScope;
import com.intern.hub.pm.model.document.DocumentType;
import com.intern.hub.pm.model.project.Project;
import com.intern.hub.pm.model.team.Team;
import com.intern.hub.pm.model.team.TeamMember;
import com.intern.hub.pm.repository.ProjectRepository;
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
        
        List<Long> leadIds = teams.stream()
                .map(Team::getAssigneeId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        
        Map<Long, String> leadNameMap = new HashMap<>();
        if (!leadIds.isEmpty()) {
            try {
                var response = hrmInternalFeignClient.getUsersByIdsInternal(leadIds);
                if (response != null && response.data() != null) {
                    response.data().forEach(u -> leadNameMap.put(Long.valueOf(u.userId()), u.fullName()));
                }
            } catch (Exception e) {
                // Keep default IDs if HRM call fails
            }
        }

        return PaginatedData.<TeamResponse>builder()
                .items(teams.stream()
                        .map(t -> toResponseWithLead(t, leadNameMap.getOrDefault(t.getAssigneeId(), "Lead (ID: " + t.getAssigneeId() + ")")))
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
                files
        );

        return toResponse(savedTeam);
    }

    @Override
    @Transactional
    public TeamResponse updateTeam(Long teamId, TeamUpsertRequest request, List<MultipartFile> files) {
        Team team = getActiveTeam(teamId);
        assertTeamOwner(team);
        validateDateRange(request.startDate(), request.endDate());

        if (team.getStatus() != StatusWork.NOT_STARTED) {
            throw new ConflictDataException("Chi duoc sua khi team chua bat dau");
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
                files
        );

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
    public TeamResponse completeTeam(Long teamId, TeamCompleteRequest request) {
        Team team = getActiveTeam(teamId);
        assertTeamOwner(team);
        team.setStatus(StatusWork.PENDING_REVIEW);
        team.setCompletionComment(trimToNull(request.completionComment()));
        return toResponse(teamRepository.save(team));
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
            throw new ForbiddenException("Bạn không phải là leader team này!");
        }
    }

    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ConflictDataException("Ngày bắt đầu phải trước ngày kết thúc");
        }
    }

    private TeamResponse toResponse(Team team) {
        String leadName = "Lead (ID: " + team.getAssigneeId() + ")";
        if (team.getAssigneeId() != null) {
            try {
                var response = hrmInternalFeignClient.getUserByIdInternal(team.getAssigneeId());
                if (response != null && response.data() != null) {
                    leadName = response.data().fullName();
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        return toResponseWithLead(team, leadName);
    }

    private TeamResponse toResponseWithLead(Team team, String leadName) {
        List<DocumentResponse> charterDocuments = documentService.getDocuments(
                team.getId(),
                DocumentScope.TEAM,
                DocumentType.CHARTER
        );

        Integer memberCount = teamMemberRepository.countByTeamIdAndStatus(team.getId(), com.intern.hub.pm.model.constant.Status.ACTIVE);

        return new TeamResponse(
                team.getId(),
                team.getTeamUUID(),
                team.getName(),
                team.getDescription(),
                team.getNote(),
                team.getStatus(),
                team.getBudgetToken(),
                team.getRewardToken(),
                team.getCreatorId(),
                team.getAssigneeId(),
                team.getProject() != null ? team.getProject().getId() : null,
                team.getDeliverableDescription(),
                team.getDeliverableLink(),
                team.getCompletionComment(),
                team.getStartDate(),
                team.getEndDate(),
                charterDocuments,
                leadName,
                memberCount != null ? memberCount : 0,
                team.getCreatedAt(),
                team.getUpdatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getTeamMembers(Long teamId) {
        List<TeamMember> members = teamMemberRepository.findAllByTeamId(teamId);
        if (members.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = members.stream()
                .map(TeamMember::getUserId)
                .toList();

        Map<Long, HrmUserClientModel> userMap = new HashMap<>();
        try {
            var response = hrmInternalFeignClient.getUsersByIdsInternal(userIds);
            if (response != null && response.data() != null) {
                response.data().forEach(u -> {
                    try {
                        userMap.put(Long.valueOf(u.userId()), u);
                    } catch (NumberFormatException e) {
                        // Skip or handle non-numeric user IDs if any
                    }
                });
            }
        } catch (Exception e) {
            // Log error or handle
        }

        return members.stream()
                .map(m -> {
                    HrmUserClientModel u = userMap.get(m.getUserId());
                    return new TeamMemberResponse(
                            m.getId(),
                            m.getUserId(),
                            u != null ? u.fullName() : "User (ID: " + m.getUserId() + ")",
                            u != null ? u.email() : null,
                            m.getStatus()
                    );
                })
                .toList();
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
}
