package com.intern.hub.pm.repository.specification;

import com.intern.hub.pm.dto.request.EntityMemberFilterRequest;
import com.intern.hub.pm.model.EntityMember;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EntityMemberSpecification {

    public static Specification<EntityMember> filter(EntityMemberFilterRequest filter) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // name
            if (filter.getEntityType() != null) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("entity_type")),
                                "%" + filter.getEntityType() + "%"
                        )
                );
            }

            if (filter.getEntityId() != null) {
                predicates.add(
                        cb.equal(root.get("entity_id").get("id"), filter.getEntityId())
                );
            }

            // pmId
            if (filter.getUserId() != null) {
                predicates.add(
                        cb.equal(root.get("user").get("id"), filter.getUserId())
                );
            }

            // status
            if (filter.getStatus() != null) {
                predicates.add(
                        cb.equal(root.get("status"), filter.getStatus())
                );
            }

            if (filter.getRole() != null) {
                predicates.add(
                        cb.equal(root.get("role"), filter.getRole())
                );
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

