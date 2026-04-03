package com.intern.hub.pm.repository;

import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.team.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    long countByTeamIdAndStatus(Long teamId, StatusWork status);

    long countByTeamIdAndStatusNot(Long teamId, StatusWork status);

    long countByTeamIdAndStatusNotIn(Long teamId, List<StatusWork> statuses);

    @Query("SELECT t.assigneeId, COUNT(t) FROM Task t WHERE t.team.id = :teamId AND t.assigneeId IN :userIds AND t.status <> :status GROUP BY t.assigneeId")
    List<Object[]> countTasksByTeamIdAndAssigneeIds(@Param("teamId") Long teamId, @Param("userIds") List<Long> userIds, @Param("status") StatusWork status);

    long countByTeamIdAndAssigneeIdAndStatusNot(Long teamId, Long assigneeId, StatusWork status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.team.project.id = :projectId AND t.assigneeId = :assigneeId AND t.status <> :status")
    long countByProjectIdAndAssigneeIdAndStatusNot(@Param("projectId") Long projectId, @Param("assigneeId") Long assigneeId, @Param("status") StatusWork status);

}
