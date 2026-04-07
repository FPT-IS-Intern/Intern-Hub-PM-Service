package com.intern.hub.pm.repository;

import com.intern.hub.pm.model.document.Document;
import com.intern.hub.pm.model.document.DocumentScope;
import com.intern.hub.pm.model.document.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findAllByEntityIdAndDocumentScopeAndDocumentType(Long entityId,
                                                                    DocumentScope documentScope,
                                                                    DocumentType documentType);

    List<Document> findAllByEntityIdInAndDocumentScope(List<Long> entityIds, DocumentScope documentScope);

    void deleteAllByEntityIdAndDocumentScopeAndDocumentType(Long entityId,
                                                            DocumentScope documentScope,
                                                            DocumentType documentType);
}
