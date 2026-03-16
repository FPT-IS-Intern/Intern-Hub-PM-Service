package com.intern.hub.pm.service.impl;

import com.intern.hub.pm.dto.request.EditRoleUserRequest;
import com.intern.hub.pm.dto.request.UserProjectRequest;
import com.intern.hub.pm.dto.response.ProjectUserResponse;
import com.intern.hub.pm.enums.Status;
import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.library.common.exception.NotFoundException;
import com.intern.hub.pm.model.common.EntityMember;
import com.intern.hub.pm.model.Project;
import com.intern.hub.pm.repository.EntityMemberRepository;
import com.intern.hub.pm.repository.WorkItemRepository;
import com.intern.hub.pm.service.IEntityMemberService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EntityMemberService implements IEntityMemberService {

    private final HrmUserDirectoryService hrmUserDirectoryService;
    private final WorkItemRepository workItemRepository;
    private final EntityMemberRepository entityMemberRepository;

    @Transactional
    public void saveListUserProject(Long id, List<UserProjectRequest> requests) {
        Project project = workItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("project.not.found", "Không tìm thấy dự án id: " + id));

        for (UserProjectRequest req : requests) {
            var user = hrmUserDirectoryService.requireById(req.getId());

            if (entityMemberRepository.existsByEntityTypeAndEntityId_IdAndUserIdAndRoleAndStatus(
                    WorkItemType.PROJECT, id, user.userId(), req.getRole(), Status.ACTIVE)) {
                throw new NotFoundException("project.member.duplicated", "User : " + user.fullName() + " đã tồn tại trong dự án");
            }

            EntityMember e = new EntityMember();
            e.setEntityType(WorkItemType.PROJECT);
            e.setEntityId(project);
            e.setUserId(user.userId());
            e.setRole(req.getRole());
            e.setStatus(Status.ACTIVE);
            e.setCreatedAt(LocalDateTime.now());
            e.setUpdatedAt(LocalDateTime.now());
            entityMemberRepository.save(e);
        }
    }

    @Override
    public Page<ProjectUserResponse> projectUserList(Long projectId, WorkItemType workItemType, int page, int size) {
        Project project = workItemRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("project.not.found", "Không tìm thấy dự án id: " + projectId));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<EntityMember> projectUserPage = entityMemberRepository.findByEntityTypeAndEntityId_IdAndStatus(
                workItemType,
                project.getId(),
                Status.ACTIVE,
                pageable
        );

        List<Object[]> results =
                workItemRepository.countTaskByProjectGroupByUser(projectId, WorkItemType.TASK, StatusWork.CANCELED);

        Map<Long, Long> taskCountMap = results.stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));

        var userMap = hrmUserDirectoryService.findByIds(projectUserPage.stream().map(EntityMember::getUserId).toList());

        return projectUserPage.map(pu -> {
            var memberUser = userMap.get(pu.getUserId());
            long count = taskCountMap.getOrDefault(pu.getUserId(), 0L);
            return ProjectUserResponse.builder()
                    .id(pu.getId())
                    .idUser(pu.getUserId())
                    .name(memberUser != null ? memberUser.fullName() : null)
                    .role(pu.getRole())
                    .tasksCount(count)
                    .createdAt(pu.getCreatedAt())
                    .build();
        });
    }

    @Override
    public EntityMember findById(Long id) {
        return entityMemberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("project.member.not.found", "Không tìm thấy user trong dự án"));
    }

    @Override
    public void deleteByProjectId(Long entityId) {
        entityMemberRepository.deleteByEntityTypeAndEntityId_Id(WorkItemType.PROJECT, entityId);
    }

    @Override
    public void deleteUserOfProject(Long id) throws NotFoundException {
        EntityMember entityMember = findById(id);

        Project project = workItemRepository.findById(entityMember.getEntityId().getId())
                .orElseThrow(() -> new NotFoundException("project.not.found", "Không tìm thấy dự án có id: " + id));

        Optional<Project> workItemOfUser = workItemRepository.findByParentAndAssigneeId(project, entityMember.getUserId());

        if (workItemOfUser.isPresent() && workItemOfUser.get().getStatus() != StatusWork.CANCELED) {
            throw new NotFoundException("project.member.delete.forbidden", "User này có dự án đang làm nên không xóa được!");
        }

        entityMember.setStatus(Status.DELETED);
        entityMember.setUpdatedAt(LocalDateTime.now());
        entityMemberRepository.save(entityMember);
    }

    @Override
    public void editRoleUser(Long id, EditRoleUserRequest request) {
        EntityMember entityMember = findById(id);
        entityMember.setRole(request.getRole());
        entityMember.setUpdatedAt(LocalDateTime.now());
        entityMemberRepository.save(entityMember);
    }

    @Override
    public long countMemberOfWork(Long entityId, Status status) {
        return entityMemberRepository.countByEntityId_IdAndStatus(entityId, status);
    }
}

