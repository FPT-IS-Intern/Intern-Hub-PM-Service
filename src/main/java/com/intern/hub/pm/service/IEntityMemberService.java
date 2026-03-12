package com.intern.hub.pm.service;

import com.intern.hub.pm.dto.request.EditRoleUserRequest;
import com.intern.hub.pm.dto.response.ProjectUserResponse;
import com.intern.hub.pm.enums.Status;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.model.EntityMember;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;

public interface IEntityMemberService {

    Page<ProjectUserResponse> projectUserList(
            Long projectId,
            WorkItemType workItemType,
            int page,
            int size
    );

    EntityMember findById(Long id);

    void deleteByProjectId(Long id);

    void deleteUserOfProject(Long id) throws BadRequestException;

    void editRoleUser(Long id, EditRoleUserRequest request);

    long countMemberOfWork(Long entityId, Status status);

}

