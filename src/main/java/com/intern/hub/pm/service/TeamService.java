package com.intern.hub.pm.service;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.pm.dto.project.ApproveRequest;
import com.intern.hub.pm.dto.team.TeamCompleteRequest;
import com.intern.hub.pm.dto.team.TeamResponse;
import com.intern.hub.pm.dto.team.TeamUpsertRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TeamService {

    PaginatedData<TeamResponse> getTeams(int page, int size);

    TeamResponse getTeam(Long teamId);

    TeamResponse createTeam(Long userId, TeamUpsertRequest request, List<MultipartFile> files);

    TeamResponse updateTeam(Long teamId, TeamUpsertRequest request, List<MultipartFile> files);

    void deleteTeam(Long teamId);

    TeamResponse completeTeam(Long teamId, TeamCompleteRequest request);

    TeamResponse approveTeam(Long teamId, ApproveRequest request);
}
