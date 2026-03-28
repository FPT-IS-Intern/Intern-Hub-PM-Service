package com.intern.hub.pm.service;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.pm.dto.project.member.ProjectMemberCreateRequest;
import com.intern.hub.pm.dto.project.member.ProjectMemberResponse;
import com.intern.hub.pm.dto.project.member.ProjectMemberUpdateRequest;
import com.intern.hub.pm.feign.model.HrmFilterRequest;
import com.intern.hub.pm.feign.model.HrmFilterResponse;

import java.util.List;

public interface ProjectMemberService {

    List<ProjectMemberResponse> addMembers(Long projectId, List<ProjectMemberCreateRequest> requests);

    PaginatedData<ProjectMemberResponse> getMembers(Long projectId, int page, int size);

    ProjectMemberResponse updateMember(Long memberId, ProjectMemberUpdateRequest request);

    void deleteMember(Long memberId);

    ResponseApi<PaginatedData<HrmFilterResponse>> searchProjectMembers(Long projectId, HrmFilterRequest request, int page, int size);
}
