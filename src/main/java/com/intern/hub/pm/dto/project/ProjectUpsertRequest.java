package com.intern.hub.pm.dto.project;

import com.intern.hub.pm.dto.project.member.ProjectMemberCreateRequest;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.project.ProjectMember;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectUpsertRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @Min(0) Long budgetToken,
        @NotNull @Min(0) Long rewardToken,
        @NotNull Long assigneeId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        List<ProjectMemberCreateRequest> memberList
) {
}
