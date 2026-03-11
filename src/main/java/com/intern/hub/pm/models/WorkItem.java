package com.intern.hub.pm.models;

import com.intern.hub.pm.enums.StatusWork;
import com.intern.hub.pm.enums.WorkItemType;
import com.intern.hub.pm.generator.SnowflakeGenerated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "work_items")
public class WorkItem {

    @Id
    @SnowflakeGenerated
    private Long id;

    @Column(name = "work_item_uuid")
    private String workItemUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private WorkItem parent;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private WorkItemType type;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "assignee_id")
    private Long assigneeId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "work_status")
    private StatusWork status;

    @Column(name = "result")
    private String result;

    @Column(name = "result_link")
    private String resultLink;

    @Column(name = "note")
    private String note;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
