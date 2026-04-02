package com.intern.hub.pm.repository;

import com.intern.hub.pm.model.constant.Status;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.project.ProjectMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    Page<ProjectMember> findAllByProjectIdAndStatus(Long projectId, Status status, Pageable pageable);

    List<ProjectMember> findAllByProjectIdAndStatusOrderByCreatedAtAsc(Long projectId, Status status);

    Optional<ProjectMember> findByIdAndStatus(Long id, Status status);

    boolean existsByProjectIdAndUserIdAndStatus(Long projectId, Long userId, Status status);

    Optional<ProjectMember> findByProjectIdAndUserIdAndStatus(Long projectId, Long userId, Status status);

    long countByProjectIdAndStatus(Long projectId, Status status);

    @Query("SELECT pm.project.id, COUNT(pm) FROM ProjectMember pm WHERE pm.project.id IN :projectIds AND pm.status = :status GROUP BY pm.project.id")
    List<Object[]> countMembersByProjectIds(@Param("projectIds") List<Long> projectIds, @Param("status") Status status);

    @Query("SELECT pm.userId FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.status = :status")
    List<Long> findUserIdsByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") Status status);

    @Query("""
            SELECT COUNT(DISTINCT pm.project.id)
            FROM ProjectMember pm
            WHERE pm.userId = :userId
              AND pm.status = :memberStatus
              AND pm.project.status <> :projectStatus
            """)
    Long countActiveProjectsByUserId(@Param("userId") Long userId,
                                     @Param("memberStatus") Status memberStatus,
                                     @Param("projectStatus") StatusWork projectStatus);

    @Query("""
            SELECT pm.userId, COUNT(DISTINCT pm.project.id)
            FROM ProjectMember pm
            WHERE pm.userId IN :userIds
              AND pm.status = :memberStatus
              AND pm.project.status <> :projectStatus
            GROUP BY pm.userId
            """)
    List<Object[]> countActiveProjectsByUserIds(@Param("userIds") List<Long> userIds,
                                                @Param("memberStatus") Status memberStatus,
                                                @Param("projectStatus") StatusWork projectStatus);
}
