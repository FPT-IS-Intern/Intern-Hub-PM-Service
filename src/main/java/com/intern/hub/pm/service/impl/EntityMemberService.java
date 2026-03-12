package com.intern.hub.pm.service.impl;

import com.intern.hub.pm.dto.request.EditRoleUserRequest;
import com.intern.hub.pm.dto.request.UserProjectRequest;
import com.intern.hub.pm.dto.response.ProjectUserResponse;
import com.intern.hub.pm.enums.Status;
import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.exceptions.NotFoundException;
import com.intern.hub.pm.model.EntityMember;
import com.intern.hub.pm.model.User;
import com.intern.hub.pm.model.WorkItem;
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

    private final UserService userService;
    private final WorkItemRepository workItemRepository;
    private final EntityMemberRepository entityMemberRepository;

    @Transactional
    public void saveListUserProject(Long id, List<UserProjectRequest> requests) {
        WorkItem workItem = workItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dự án id: " + id));

        for (UserProjectRequest req : requests) {
            User user = userService.findById(req.getId());

            if (entityMemberRepository.existsByEntityTypeAndEntityId_IdAndUserIdAndRoleAndStatus(
                    WorkItemType.PROJECT, id, user.getId(), req.getRole(), Status.ACTIVE)) {
                throw new NotFoundException("User : " + user.getFullName() + " đã tồn tại trong dự án");
            }

            EntityMember e = new EntityMember();
            e.setEntityType(WorkItemType.PROJECT);
            e.setEntityId(workItem);
            e.setUserId(user.getId());
            e.setRole(req.getRole());
            e.setStatus(Status.ACTIVE);
            e.setCreatedAt(LocalDateTime.now());
            e.setUpdatedAt(LocalDateTime.now());
            entityMemberRepository.save(e);
        }
    }

    @Transactional
    public void saveListUserModule(Long id, List<UserProjectRequest> requests) {
        WorkItem workItem = workItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dự án id: " + id));

        for (UserProjectRequest req : requests) {
            User user = userService.findById(req.getId());

            if (entityMemberRepository.existsByEntityTypeAndEntityId_IdAndUserIdAndRoleAndStatus(
                    WorkItemType.MODULE, id, user.getId(), req.getRole(), Status.ACTIVE)) {
                throw new NotFoundException("User : " + user.getFullName() + " đã tồn tại trong module");
            }

            EntityMember e = new EntityMember();
            e.setEntityType(WorkItemType.MODULE);
            e.setEntityId(workItem);
            e.setUserId(user.getId());
            e.setRole(req.getRole());
            e.setStatus(Status.ACTIVE);
            e.setCreatedAt(LocalDateTime.now());
            e.setUpdatedAt(LocalDateTime.now());
            entityMemberRepository.save(e);
        }
    }

    @Override
    public Page<ProjectUserResponse> projectUserList(Long projectId, WorkItemType workItemType, int page, int size) {
        WorkItem project = workItemRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dự án id: " + projectId));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<EntityMember> projectUserPage = entityMemberRepository.findByEntityTypeAndEntityId_IdAndStatus(
                workItemType,
                project.getId(),
                Status.ACTIVE,
                pageable
        );

        List<Object[]> results;
        if (workItemType.equals(WorkItemType.PROJECT)) {
            results = workItemRepository.countTaskByProjectGroupByUser(projectId, WorkItemType.MODULE, StatusWork.DA_XOA);
        } else {
            results = workItemRepository.countTaskByProjectGroupByUser(projectId, WorkItemType.TASK, StatusWork.DA_XOA);
        }

        Map<Long, Long> taskCountMap = results.stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));

        return projectUserPage.map(pu -> {
            User memberUser = userService.findById(pu.getUserId());
            long count = taskCountMap.getOrDefault(pu.getUserId(), 0L);
            return ProjectUserResponse.builder()
                    .id(pu.getId())
                    .idUser(memberUser.getId())
                    .name(memberUser.getFullName())
                    .role(pu.getRole())
                    .tasksCount(count)
                    .createdAt(pu.getCreatedAt())
                    .build();
        });
    }

    @Override
    public EntityMember findById(Long id) {
        return entityMemberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy user trong dự án"));
    }

    @Override
    public void deleteByProjectId(Long entityId) {
        entityMemberRepository.deleteByEntityTypeAndEntityId_Id(WorkItemType.PROJECT, entityId);
    }

    @Override
    public void deleteUserOfProject(Long id) throws NotFoundException {
        EntityMember entityMember = findById(id);

        WorkItem workItem = workItemRepository.findById(entityMember.getEntityId().getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dự án có id: " + id));

        Optional<WorkItem> workItemOfUser = workItemRepository.findByParentAndAssigneeId(workItem, entityMember.getUserId());

        if (workItemOfUser.isPresent() && workItemOfUser.get().getStatus() != StatusWork.DA_XOA) {
            throw new NotFoundException("User này có dự án đang làm nên không xóa được!");
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

