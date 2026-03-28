package com.intern.hub.pm.repository;

import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TeamRepository extends JpaRepository<Team, Long>, JpaSpecificationExecutor<Team> {

    long countByProjectIdAndStatusNot(Long projectId, StatusWork status);
    long countByStatusNot(StatusWork status);

    long countByProjectIdAndStatus(Long projectId, StatusWork status);
    long countByStatus(StatusWork status);
}
