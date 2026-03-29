package com.intern.hub.pm.repository;

import com.intern.hub.pm.model.constant.Status;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.team.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    Integer countByTeamIdAndStatus(Long teamId, Status status);
    List<TeamMember> findAllByTeamId(Long teamId);

    @Query("""
            SELECT COUNT(DISTINCT tm.team.id)
            FROM TeamMember tm
            WHERE tm.userId = :userId
              AND tm.status = :memberStatus
              AND tm.team.status <> :teamStatus
              AND tm.team.project.id = :projectId
            """)
    Long countActiveTeamsByUserId(@Param("userId") Long userId,
                                  @Param("memberStatus") Status memberStatus,
                                  @Param("teamStatus") StatusWork teamStatus,
                                  @Param("projectId") Long projectId);

    @Query("""
            SELECT tm.userId, COUNT(DISTINCT tm.team.id)
            FROM TeamMember tm
            WHERE tm.userId IN :userIds
              AND tm.status = :memberStatus
              AND tm.team.status <> :teamStatus
              AND tm.team.project.id = :projectId
            GROUP BY tm.userId
            """)
    List<Object[]> countActiveTeamsByUserIds(@Param("userIds") List<Long> userIds,
                                             @Param("memberStatus") Status memberStatus,
                                             @Param("teamStatus") StatusWork teamStatus,
                                             @Param("projectId") Long projectId);
}
