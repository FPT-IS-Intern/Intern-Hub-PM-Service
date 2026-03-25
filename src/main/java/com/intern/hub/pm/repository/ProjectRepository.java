package com.intern.hub.pm.repository;

import com.intern.hub.pm.model.project.Project;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByStatusNot(com.intern.hub.pm.model.constant.StatusWork status, Sort sort);
    org.springframework.data.domain.Page<Project> findAllByStatusNot(com.intern.hub.pm.model.constant.StatusWork status, org.springframework.data.domain.Pageable pageable);
}
