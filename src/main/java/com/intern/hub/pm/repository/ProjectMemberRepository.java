package com.intern.hub.pm.repository;

import com.intern.hub.pm.model.constant.Status;
import com.intern.hub.pm.model.project.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    org.springframework.data.domain.Page<ProjectMember> findAllByProjectIdAndStatus(Long projectId, Status status, org.springframework.data.domain.Pageable pageable);

    List<ProjectMember> findAllByProjectIdAndStatusOrderByCreatedAtAsc(Long projectId, Status status);

    Optional<ProjectMember> findByIdAndStatus(Long id, Status status);

    boolean existsByProjectIdAndUserIdAndStatus(Long projectId, Long userId, Status status);

    @Query("SELECT pm.userId FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.status = :status")
    List<Long> findUserIdsByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") Status status);
}
