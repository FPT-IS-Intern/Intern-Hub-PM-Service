package com.intern.hub.pm.repository;

import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkItemRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    Page<Project> findByType(WorkItemType type, Pageable pageable);

    Page<Project> findByParentIdAndType(Long parentId, WorkItemType type, Pageable pageable);

    Optional<Project> findByParentAndAssigneeId(Project parentId, Long assigneeId);

    List<Project> findByParent_IdAndTypeAndStatus(Long parentId, WorkItemType type, StatusWork status);

    boolean existsByParent_IdAndTypeAndStatus(Long parentId, WorkItemType type, StatusWork status);

    boolean existsByParent_IdAndStatus(Long parentId, StatusWork status);

    boolean existsByParent_IdAndTypeAndStatusNotIn(Long parentId, WorkItemType type, List<StatusWork> statuses);

    @Query("""
            SELECT COUNT(w)
            FROM Project w
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
            FROM Project w
            WHERE w.parent.id = :projectId
              AND w.type = :type
              AND w.status <> :deletedStatus
            GROUP BY w.assigneeId
            """)
    List<Object[]> countTaskByProjectGroupByUser(Long projectId, WorkItemType type, StatusWork deletedStatus);
}
