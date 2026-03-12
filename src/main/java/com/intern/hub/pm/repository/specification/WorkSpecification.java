package com.intern.hub.pm.repository.specification;

import com.intern.hub.pm.dtos.request.WorkFilterRequest;
import com.intern.hub.pm.model.WorkItem;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class WorkSpecification {

    public static Specification<WorkItem> filter(WorkFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getParentId() != null) {
                predicates.add(cb.equal(root.get("parent").get("id"), filter.getParentId()));
            }
            if (filter.getName() != null && !filter.getName().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
            }
            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (filter.getStatusNot() != null) {
                predicates.add(cb.notEqual(root.get("status"), filter.getStatusNot()));
            }
            if (filter.getAssignee() != null) {
                predicates.add(cb.equal(root.get("assigneeId"), filter.getAssignee()));
            }
            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }
            if (filter.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), filter.getStartDate().atStartOfDay()));
            }
            if (filter.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), filter.getEndDate().atTime(23, 59, 59)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
