package com.intern.hub.pm.model.team;

import com.intern.hub.pm.enums.Status;
import com.intern.hub.pm.generator.SnowflakeGenerated;
import com.intern.hub.pm.model.AuditEntity;
import com.intern.hub.pm.model.project.Project;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "team_members")
public class TeamMember extends AuditEntity {

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

    /**
     * =======================================================
     * Relationship
     * =======================================================
     */

    @ManyToOne
    @JoinColumn(name = "team_id")
    Team team;

}
