package com.intern.hub.pm.repository;

import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.team.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Page<Task> findAllByProjectIdAndStatusNot(Long projectId, StatusWork status, Pageable pageable);

    Page<Task> findAllByAssigneeIdAndStatusNot(Long assigneeId, StatusWork status, Pageable pageable);

    long countByTeamId(Long teamId);

    long countByTeamIdAndStatus(Long teamId, StatusWork status);

    long countByTeamIdAndStatusNot(Long teamId, StatusWork status);
}
