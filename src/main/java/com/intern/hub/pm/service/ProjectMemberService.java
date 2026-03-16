package com.intern.hub.pm.service;

import com.intern.hub.pm.dto.project.member.ProjectMemberCreateRequest;
import com.intern.hub.pm.dto.project.member.ProjectMemberResponse;
import com.intern.hub.pm.dto.project.member.ProjectMemberUpdateRequest;

import java.util.List;

public interface ProjectMemberService {

    ProjectMemberResponse addMember(Long projectId, ProjectMemberCreateRequest request);

    List<ProjectMemberResponse> getMembers(Long projectId);

    ProjectMemberResponse updateMember(Long memberId, ProjectMemberUpdateRequest request);

    void deleteMember(Long memberId);
}
