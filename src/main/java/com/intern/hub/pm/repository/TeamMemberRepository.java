package com.intern.hub.pm.repository;

import com.intern.hub.pm.model.constant.Status;
import com.intern.hub.pm.model.team.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    Integer countByTeamIdAndStatus(Long teamId, Status status);
    List<TeamMember> findAllByTeamId(Long teamId);
}
