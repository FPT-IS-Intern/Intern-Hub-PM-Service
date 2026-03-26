package com.intern.hub.pm.repository;

import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.team.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Page<Team> findAllByStatusNot(StatusWork status, Pageable pageable);
}
