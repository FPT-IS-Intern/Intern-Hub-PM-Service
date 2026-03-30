package com.intern.hub.pm.service;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.pm.dto.team.TeamMemberCreateRequest;
import com.intern.hub.pm.dto.team.TeamMemberResponse;

import java.util.List;

public interface TeamMemberService {
    List<TeamMemberResponse> addMembers(Long teamId, List<TeamMemberCreateRequest> requests);
    PaginatedData<TeamMemberResponse> getMembers(Long teamId, String keyword, int page, int size);
    void deleteMember(Long memberId);
}
