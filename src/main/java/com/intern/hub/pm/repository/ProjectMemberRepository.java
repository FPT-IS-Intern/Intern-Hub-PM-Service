package com.intern.hub.pm.repository;

import com.intern.hub.pm.model.constant.Status;
import com.intern.hub.pm.model.project.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findAllByProjectIdAndStatusOrderByCreatedAtAsc(Long projectId, Status status);

    Optional<ProjectMember> findByIdAndStatus(Long id, Status status);

    boolean existsByProjectIdAndUserIdAndStatus(Long projectId, Long userId, Status status);
}
