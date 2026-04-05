package com.intern.hub.pm.model.project;

import com.intern.hub.pm.model.constant.StatusWork;
import com.intern.hub.pm.generator.SnowflakeGenerated;
import com.intern.hub.pm.model.AuditEntity;
import com.intern.hub.pm.model.document.Document;
import com.intern.hub.pm.model.team.Team;
import com.intern.hub.pm.model.team.Task;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "projects")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
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
    BigInteger budgetToken;
    
    @Column(nullable = false)
    BigInteger rewardToken;

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
     * Document Project Charter - DMS MODULE
     * =======================================================
     */

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "entity_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    List<Document> projectCharterDocuments;

    /**
     * =======================================================
     * Document Deliverables - DMS MODULE
     * =======================================================
     */

    String deliverableDescription; // đáp án

    String deliverableLink;  //link

    String completionComment; // nhận xét

    LocalDateTime startDate;

    LocalDateTime endDate;
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
