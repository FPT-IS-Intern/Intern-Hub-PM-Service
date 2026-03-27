package com.intern.hub.pm.repository;

import com.intern.hub.pm.model.team.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
}
