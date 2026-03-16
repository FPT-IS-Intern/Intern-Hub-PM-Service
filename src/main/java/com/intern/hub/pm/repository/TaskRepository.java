package com.intern.hub.pm.repository;

import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.team.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByProjectIdAndStatusNotOrderByCreatedAtDesc(Long projectId, StatusWork status);

    List<Task> findAllByAssigneeIdAndStatusNotOrderByCreatedAtDesc(Long assigneeId, StatusWork status);
}
