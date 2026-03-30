package com.intern.hub.pm.repository;

import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long>, JpaSpecificationExecutor<Team> {

    long countByProjectIdAndStatusNot(Long projectId, StatusWork status);
    long countByStatusNot(StatusWork status);

    long countByProjectIdAndStatus(Long projectId, StatusWork status);
    long countByStatus(StatusWork status);

    long countByAssigneeIdAndProjectIdAndStatusNot(Long assigneeId, Long projectId, StatusWork status);

    @Query("SELECT t.assigneeId, COUNT(t) FROM Team t WHERE t.assigneeId IN :userIds AND t.project.id = :projectId AND t.status <> :status GROUP BY t.assigneeId")
    List<Object[]> countTeamsByAssigneeIds(@Param("userIds") List<Long> userIds, @Param("projectId") Long projectId, @Param("status") StatusWork status);
}
