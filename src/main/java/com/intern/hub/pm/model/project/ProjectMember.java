package com.intern.hub.pm.model.project;

import com.intern.hub.pm.model.constant.Status;
import com.intern.hub.pm.generator.SnowflakeGenerated;
import com.intern.hub.pm.model.AuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "project_members")
public class ProjectMember extends AuditEntity {

    /**
     * =======================================================
     * ENTITY ID
     * =======================================================
     */

    @Id
    @SnowflakeGenerated
    private Long id;

    /**
     * =======================================================
     * ENTITY METADATA
     * =======================================================
     */

    private Long userId;

    private String role;

    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * =======================================================
     * Relationship
     * =======================================================
     */

    @ManyToOne
    @JoinColumn(name = "project_id")
    Project project;

}
