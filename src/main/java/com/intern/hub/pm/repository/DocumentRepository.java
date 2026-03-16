package com.intern.hub.pm.repository;

import com.intern.hub.pm.model.common.Document;
import com.intern.hub.pm.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByEntityId_IdAndEntityTypeAndStatus(Long entityId, String entityType, Status status);
}
