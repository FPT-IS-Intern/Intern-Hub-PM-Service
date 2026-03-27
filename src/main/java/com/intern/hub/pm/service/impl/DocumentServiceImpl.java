package com.intern.hub.pm.service.impl;

import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.pm.dto.document.DocumentResponse;
import com.intern.hub.pm.model.document.Document;
import com.intern.hub.pm.model.document.DocumentScope;
import com.intern.hub.pm.model.document.DocumentType;
import com.intern.hub.pm.repository.DocumentRepository;
import com.intern.hub.pm.repository.FileStorageRepository;
import com.intern.hub.pm.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final FileStorageRepository fileStorageRepository;
    private final StorageObjectLifecycleManager storageObjectLifecycleManager;

    @Value("${aws.s3.max-total-size}")
    private Long maxTotalSize;

    @Value("${aws.s3.allow-types.document}")
    private String allowTypesDocument;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(Long entityId, DocumentScope documentScope, DocumentType documentType) {
        return documentRepository.findAllByEntityIdAndDocumentScopeAndDocumentType(entityId, documentScope, documentType)
                .stream()
                .map(document -> new DocumentResponse(
                        document.getId(),
                        document.getFileName(),
                        document.getFileUrl(),
                        document.getCreatedAt()
                ))
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
            String key = existingDocument.getFileUrl();
            if (key != null && !key.isBlank()) {
                storageObjectLifecycleManager.deleteAfterCommit(key, actorId);
            }
        }
        if (!existingDocuments.isEmpty()) {
            documentRepository.deleteAll(existingDocuments);
        }

        if (files == null || files.isEmpty()) {
            return;
        }

        long totalUploadSize = files.stream().mapToLong(MultipartFile::getSize).sum();
        if (totalUploadSize > maxTotalSize) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tổng dung lượng file vượt quá giới hạn " + (maxTotalSize / 1024 / 1024) + "MB");
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String s3Key = fileStorageRepository.uploadFile(
                    file,
                    destinationPath,
                    actorId,
                    maxTotalSize,
                    allowTypesDocument
            );
            storageObjectLifecycleManager.cleanupOnRollback(s3Key, actorId);

            documentRepository.save(Document.builder()
                    .documentScope(documentScope)
                    .documentType(documentType)
                    .entityId(entityId)
                    .fileName(file.getOriginalFilename())
                    .fileUrl(s3Key)
                    .build());
        }
    }
}
