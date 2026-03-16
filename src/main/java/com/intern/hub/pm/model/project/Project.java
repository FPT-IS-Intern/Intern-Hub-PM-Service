package com.intern.hub.pm.model.project;

import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.generator.SnowflakeGenerated;
import com.intern.hub.pm.model.AuditEntity;
import com.intern.hub.pm.model.document.Document;
import com.intern.hub.pm.model.team.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Table(name = "projects")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Project extends AuditEntity {

    /**
     * =======================================================
     * ENTITY ID
     * =======================================================
     */

    @Id
    @SnowflakeGenerated
    Long id;

    @Column(name = "project_uuid", nullable = false)
    String projectUUID;

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
    List<Document> projectCharterDocuments;

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

    @OneToMany(mappedBy = "project")
    List<ProjectMember> projectMembers;

    @OneToMany(mappedBy = "project")
    List<Team> projectTeams;

}
