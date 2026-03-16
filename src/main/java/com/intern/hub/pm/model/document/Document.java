package com.intern.hub.pm.model.document;

import com.intern.hub.pm.generator.SnowflakeGenerated;
import com.intern.hub.pm.model.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "documents")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Document extends AuditEntity {

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
     * Entity Metadata
     * =======================================================
     */

    @Enumerated(EnumType.STRING)
    DocumentType documentType;

    @Enumerated(EnumType.STRING)
    DocumentScope documentScope;

    @Column(name = "entity_id")
    Long entityId;

    @Column(name = "file_url")
    String fileUrl;

    @Column(name = "file_name")
    String fileName;

}
