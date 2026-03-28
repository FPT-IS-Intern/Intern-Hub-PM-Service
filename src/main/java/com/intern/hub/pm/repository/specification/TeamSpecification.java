package com.intern.hub.pm.repository.specification;

import com.intern.hub.pm.dto.team.TeamFilterRequest;
import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.model.team.Team;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TeamSpecification {

    public static Specification<Team> filter(TeamFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude CANCELED teams
            predicates.add(criteriaBuilder.notEqual(root.get("status"), StatusWork.CANCELED));

            if (filter.getProjectId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("project").get("id"), filter.getProjectId()));
            }

            if (StringUtils.hasText(filter.getName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%"
                ));
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

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
