package com.intern.hub.pm.repository.specification;

import com.intern.hub.pm.dto.project.ProjectFilterRequest;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.project.Project;
import com.intern.hub.pm.model.project.ProjectMember;
import com.intern.hub.pm.model.constant.Status;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProjectSpecification {

    public static Specification<Project> filter(ProjectFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude CANCELED projects unless explicitly requested or handled
            // elsewhere
            predicates.add(criteriaBuilder.notEqual(root.get("status"), StatusWork.CANCELED));

            if (StringUtils.hasText(filter.getName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%"));
            }

            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), filter.getStartDate()));
            }

            if (filter.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), filter.getEndDate()));
            }

            if (filter.getUserId() != null) {
                Long userId = filter.getUserId();

                Predicate isCreator = criteriaBuilder.equal(root.get("creatorId"), userId);

                Predicate isAssignee = criteriaBuilder.equal(root.get("assigneeId"), userId);

                Subquery<Long> memberSubquery = query.subquery(Long.class);
                Root<ProjectMember> memberRoot = memberSubquery.from(ProjectMember.class);
                memberSubquery.select(criteriaBuilder.literal(1L));
                memberSubquery.where(
                        criteriaBuilder.equal(memberRoot.get("project"), root),
                        criteriaBuilder.equal(memberRoot.get("userId"), userId),
                        criteriaBuilder.equal(memberRoot.get("status"), Status.ACTIVE));
                Predicate isMember = criteriaBuilder.exists(memberSubquery);

                predicates.add(criteriaBuilder.or(isCreator, isAssignee, isMember));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
