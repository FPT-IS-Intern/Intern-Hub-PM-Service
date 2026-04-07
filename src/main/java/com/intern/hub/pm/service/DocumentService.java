package com.intern.hub.pm.service;

import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.model.document.DocumentScope;
import com.intern.hub.pm.model.document.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface DocumentService {

    List<DocumentResponse> getDocuments(Long entityId, DocumentScope documentScope, DocumentType documentType);

    Map<Long, Map<DocumentType, List<DocumentResponse>>> getDocumentsBatch(List<Long> entityIds,
            DocumentScope documentScope);

    void replaceDocuments(Long entityId,
            DocumentScope documentScope,
            DocumentType documentType,
            Long actorId,
            String destinationPath,
            List<MultipartFile> files);
}
