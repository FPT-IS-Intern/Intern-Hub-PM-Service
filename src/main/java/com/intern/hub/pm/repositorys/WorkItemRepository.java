package com.intern.hub.pm.repositorys;

import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.models.WorkItem;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WorkItemRepository extends JpaRepository<WorkItem, Long>, JpaSpecificationExecutor<WorkItem> {

    Page<WorkItem> findByType(WorkItemType type, Pageable pageable);

    Page<WorkItem> findByParentIdAndType(Long parentId, WorkItemType type, Pageable pageable);

    Optional<WorkItem> findByParentAndAssigneeId(WorkItem parentId, Long assigneeId);

    List<WorkItem> findByParent_IdAndTypeAndStatus(Long parentId, WorkItemType type, StatusWork status);

    boolean existsByParent_IdAndTypeAndStatus(Long parentId, WorkItemType type, StatusWork status);

    boolean existsByParent_IdAndTypeAndStatusNotIn(Long parentId, WorkItemType type, List<StatusWork> statuses);

    @Query("""
        SELECT COUNT(w)
        FROM WorkItem w
        WHERE w.type = :type
          AND w.parent.id = :parentId
          AND w.assigneeId = :assigneeId
          AND w.status <> :deletedStatus
        """)
    long countTaskByUser(@Param("type") WorkItemType type,
                         @Param("parentId") Long parentId,
                         @Param("assigneeId") Long assigneeId,
                         @Param("deletedStatus") StatusWork deletedStatus);

    @Query("""
        SELECT w.assigneeId, COUNT(w)
        FROM WorkItem w
        WHERE w.parent.id = :projectId
          AND w.type = :type
          AND w.status <> :deletedStatus
        GROUP BY w.assigneeId
        """)
    List<Object[]> countTaskByProjectGroupByUser(Long projectId, WorkItemType type, StatusWork deletedStatus);
}
