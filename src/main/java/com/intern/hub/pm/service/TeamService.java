package com.intern.hub.pm.service;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.pm.dto.project.ApproveRequest;
import com.intern.hub.pm.dto.team.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TeamService {

    PaginatedData<TeamResponse> getTeams(Long projectId, int page, int size);

    PaginatedData<TeamResponse> getTeams(TeamFilterRequest filter, int page, int size);

    PaginatedData<TeamResponse> getMyTeams(Long projectId, int page, int size);

    TeamStatisticsResponse getTeamStatistics(Long projectId);

    TeamResponse getTeam(Long teamId);

    TeamResponse createTeam(Long userId, TeamUpsertRequest request, List<MultipartFile> files);

    TeamResponse updateTeam(Long teamId, TeamUpsertRequest request, List<MultipartFile> files);

    void deleteTeam(Long teamId);

    TeamResponse completeTeam(Long teamId, TeamCompleteRequest request, List<MultipartFile> files);

    TeamResponse approveTeam(Long teamId, ApproveRequest request);

    TeamResponse acceptTeam(Long teamId);

    TeamResponse refuseTask(Long teamId);

    TeamResponse refuseTeam(Long teamId, ApproveRequest request);
}
