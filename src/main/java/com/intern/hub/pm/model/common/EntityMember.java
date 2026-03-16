package com.intern.hub.pm.model.common;

import com.intern.hub.pm.enums.Status;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.generator.SnowflakeGenerated;
import com.intern.hub.pm.model.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "entity_members")
public class EntityMember {

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
