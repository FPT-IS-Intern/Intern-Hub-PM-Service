package com.intern.hub.pm.model.document;

import com.intern.hub.pm.generator.SnowflakeGenerated;
import com.intern.hub.pm.model.AuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "documents")
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    DocumentType documentType;

    DocumentScope documentScope;

    Long entityId;

    @Column(name = "file_url")
    String fileUrl;

    @Column(name = "file_name")
    String fileName;

}
