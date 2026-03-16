package com.intern.hub.pm.model.common;

import com.intern.hub.pm.enums.Status;
import com.intern.hub.pm.generator.SnowflakeGenerated;
import com.intern.hub.pm.model.Project;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "documents")
public class Document {


    @Id
    @SnowflakeGenerated
    private Long id;

    @Column(name = "entity_type")
    private String entityType;

    @ManyToOne
    @JoinColumn(name = "entity_id")
    private Project entityId;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
