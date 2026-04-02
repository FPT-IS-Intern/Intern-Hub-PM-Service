package com.intern.hub.pm.repository;

import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.team.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long>, JpaSpecificationExecutor<Team> {

    long countByProjectIdAndStatusNot(Long projectId, StatusWork status);

    long countByProjectIdAndStatusNotIn(Long projectId, List<StatusWork> statuses);

    long countByStatusNot(StatusWork status);

    long countByProjectIdAndStatus(Long projectId, StatusWork status);

    long countByStatus(StatusWork status);

    long countByAssigneeIdAndProjectIdAndStatusNot(Long assigneeId, Long projectId, StatusWork status);

    @Query("SELECT t.assigneeId, COUNT(t) FROM Team t WHERE t.assigneeId IN :userIds AND t.project.id = :projectId AND t.status <> :status GROUP BY t.assigneeId")
    List<Object[]> countTeamsByAssigneeIds(@Param("userIds") List<Long> userIds, @Param("projectId") Long projectId, @Param("status") StatusWork status);

//    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN t.members m WHERE t.project.id = :projectId AND (t.assigneeId = :userId OR m.userId = :userId) AND t.status <> 'CANCELED'")
//    Page<Team> findByProjectIdAndMemberUserId(@Param("projectId") Long projectId, @Param("userId") Long userId, Pageable pageable);
//

    @Query("""
                SELECT DISTINCT t
                FROM Team t
                LEFT JOIN t.teamMembers m
                WHERE t.project.id = :projectId
                  AND (t.assigneeId = :userId OR m.userId = :userId)
                  AND t.status <> 'CANCELED'
            """)
    Page<Team> findByProjectIdAndMemberUserId(
            @Param("projectId") Long projectId,
            @Param("userId") Long userId,
            Pageable pageable
    );

}
