package com.intern.hub.pm.model.project;

import com.intern.hub.pm.enums.Status;
import com.intern.hub.pm.generator.SnowflakeGenerated;
import com.intern.hub.pm.model.AuditEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
