package com.intern.hub.pm.repository;

import com.intern.hub.pm.enums.Status;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.model.common.EntityMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityMemberRepository extends JpaRepository<EntityMember, Long>, JpaSpecificationExecutor<EntityMember> {

    boolean existsByEntityTypeAndEntityId_IdAndUserIdAndRoleAndStatus(
            WorkItemType entityType,
            Long entityId,
            Long userId,
            String role,
            Status status
    );

    Page<EntityMember> findByEntityTypeAndEntityId_IdAndStatus(WorkItemType project, Long id, Status status, Pageable pageable);

    void deleteByEntityTypeAndEntityId_Id(WorkItemType entityType, Long entityId);

    long countByEntityId_IdAndStatus(Long entityId, Status status);
}
