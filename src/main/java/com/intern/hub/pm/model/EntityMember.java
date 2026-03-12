package com.intern.hub.pm.model;

import com.intern.hub.pm.enums.Status;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.generator.SnowflakeGenerated;
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

    @Id
    @SnowflakeGenerated
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type")
    private WorkItemType entityType;

    @ManyToOne
    @JoinColumn(name = "entity_id")
    private WorkItem entityId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "role")
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
