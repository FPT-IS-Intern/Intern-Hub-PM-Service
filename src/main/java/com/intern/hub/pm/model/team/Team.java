package com.intern.hub.pm.model.team;

import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.generator.SnowflakeGenerated;
import com.intern.hub.pm.model.AuditEntity;
import com.intern.hub.pm.model.document.Document;
import com.intern.hub.pm.model.project.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Table(name = "teams")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Team extends AuditEntity {

    /**
     * =======================================================
     * ENTITY ID
     * =======================================================
     */

    @Id
    @SnowflakeGenerated
    Long id;

    @Column(name = "team_uuid", nullable = false)
    String teamUUID;

    /**
     * =======================================================
     * ENTITY METADATA
     * =======================================================
     */

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String description;

    String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    StatusWork status;

    @Column(nullable = false)
    Long budgetToken;

    @Column(nullable = false)
    Long rewardToken;

    /**
     * =======================================================
     * USER ASSIGN - HRM MODULE
     * =======================================================
     */

    @Column(name = "creator_id", nullable = false)
    Long creatorId;

    @Column(name = "assignee_id", nullable = false)
    Long assigneeId;

    /**
     * =======================================================
     * Document Project Charter  - DMS MODULE
     * =======================================================
     */

    @OneToMany
    @Column(name = "assignee_id", nullable = false)
    List<Document> teamCharterDocuments;

    /**
     * =======================================================
     * Document Deliverables  - DMS MODULE
     * =======================================================
     */

    String deliverableDescription;

    String deliverableLink;

    /**
     * =======================================================
     * Relationship
     * =======================================================
     */

    @ManyToOne
    @JoinColumn(name = "project_id")
    Project project;

    @OneToMany(mappedBy = "team")
    List<TeamMember> teamMembers;

    @OneToMany(mappedBy = "team")
    List<Task> tasks;

}
