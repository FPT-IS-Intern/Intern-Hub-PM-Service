package com.intern.hub.pm.controller;

import com.intern.hub.pm.dto.project.member.ProjectMemberCreateRequest;
import com.intern.hub.pm.dto.project.member.ProjectMemberResponse;
import com.intern.hub.pm.dto.project.member.ProjectMemberUpdateRequest;
import com.intern.hub.pm.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix:/pm}/projects")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @PostMapping("/{projectId}/users")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectMemberResponse addMember(@PathVariable Long projectId,
                                           @Valid @RequestBody ProjectMemberCreateRequest request) {
        return projectMemberService.addMember(projectId, request);
    }

    @GetMapping("/{projectId}/users")
    public List<ProjectMemberResponse> getMembers(@PathVariable Long projectId) {
        return projectMemberService.getMembers(projectId);
    }

    @PutMapping("/users/{memberId}")
    public ProjectMemberResponse updateMember(@PathVariable Long memberId,
                                              @Valid @RequestBody ProjectMemberUpdateRequest request) {
        return projectMemberService.updateMember(memberId, request);
    }

    @DeleteMapping("/users/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMember(@PathVariable Long memberId) {
        projectMemberService.deleteMember(memberId);
    }
}
