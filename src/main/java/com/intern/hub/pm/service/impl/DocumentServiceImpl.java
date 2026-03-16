package com.intern.hub.pm.service.impl;

import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.feign.DmsInternalFeignClient;
import com.intern.hub.pm.feign.model.DmsDocumentClientModel;
import com.intern.hub.pm.model.document.Document;
import com.intern.hub.pm.model.document.DocumentScope;
import com.intern.hub.pm.model.document.DocumentType;
import com.intern.hub.pm.repository.DocumentRepository;
import com.intern.hub.pm.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DmsInternalFeignClient dmsInternalFeignClient;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(Long entityId, DocumentScope documentScope, DocumentType documentType) {
        return documentRepository.findAllByEntityIdAndDocumentScopeAndDocumentType(entityId, documentScope, documentType)
                .stream()
                .map(document -> new DocumentResponse(document.getId(), document.getFileName(), document.getFileUrl()))
                .toList();
    }

    @Override
    @Transactional
    public void replaceDocuments(Long entityId,
                                 DocumentScope documentScope,
                                 DocumentType documentType,
                                 Long actorId,
                                 String destinationPath,
                                 List<MultipartFile> files) {
        List<Document> existingDocuments = documentRepository.findAllByEntityIdAndDocumentScopeAndDocumentType(
                entityId, documentScope, documentType);

        for (Document existingDocument : existingDocuments) {
            if (existingDocument.getFileUrl() != null && !existingDocument.getFileUrl().isBlank()) {
                dmsInternalFeignClient.deleteFile(existingDocument.getFileUrl(), actorId);
            }
        }
        if (!existingDocuments.isEmpty()) {
            documentRepository.deleteAll(existingDocuments);
        }

        if (files == null || files.isEmpty()) {
            return;
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            ResponseApi<DmsDocumentClientModel> response = dmsInternalFeignClient.uploadFile(
                    file,
                    destinationPath,
                    actorId,
                    false
            );
            DmsDocumentClientModel uploaded = response.data();
            if (uploaded == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "DMS upload returned empty data");
            }

            documentRepository.save(Document.builder()
                    .documentScope(documentScope)
                    .documentType(documentType)
                    .entityId(entityId)
                    .fileName(uploaded.originalFileName())
                    .fileUrl(uploaded.objectKey())
                    .build());
        }
    }
}
