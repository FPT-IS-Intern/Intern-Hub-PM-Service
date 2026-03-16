package com.intern.hub.pm.service;

import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.model.document.DocumentScope;
import com.intern.hub.pm.model.document.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    List<DocumentResponse> getDocuments(Long entityId, DocumentScope documentScope, DocumentType documentType);

    void replaceDocuments(Long entityId,
                          DocumentScope documentScope,
                          DocumentType documentType,
                          Long actorId,
                          String destinationPath,
                          List<MultipartFile> files);
}
