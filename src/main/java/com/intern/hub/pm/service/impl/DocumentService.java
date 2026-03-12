package com.intern.hub.pm.service.impl;

import com.intern.hub.library.common.exception.BadRequestException;
import com.intern.hub.library.common.exception.InternalErrorException;
import com.intern.hub.pm.dto.response.DocumentResponse;
import com.intern.hub.pm.enums.Status;
import com.intern.hub.pm.feign.DmsInternalFeignClient;
import com.intern.hub.pm.model.Document;
import com.intern.hub.pm.model.WorkItem;
import com.intern.hub.pm.repository.DocumentRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DmsInternalFeignClient dmsInternalFeignClient;

    @Value("${services.dms.system-actor-id:0}")
    private Long systemActorId;

    public List<DocumentResponse> getActiveDocuments(Long workItemId, String entityType) {
        return documentRepository.findByEntityId_IdAndEntityTypeAndStatus(workItemId, entityType, Status.ACTIVE)
                .stream()
                .map(document -> DocumentResponse.builder()
                        .id(document.getId())
                        .fileName(document.getFileName())
                        .fileUrl(document.getFileUrl())
                        .build())
                .toList();
    }

    public void replaceDocuments(
            WorkItem workItem,
            String entityType,
            List<MultipartFile> files,
            Long actorId,
            String destinationPath
    ) {
        if (files == null) {
            return;
        }
        deleteDocuments(workItem.getId(), entityType, actorId);
        saveDocuments(workItem, entityType, files, actorId, destinationPath);
    }

    public void saveDocuments(
            WorkItem workItem,
            String entityType,
            List<MultipartFile> files,
            Long actorId,
            String destinationPath
    ) {
        if (files == null || files.isEmpty()) {
            return;
        }
        for (MultipartFile file : files) {
            validateFile(file);
            String objectKey = uploadToDms(file, destinationPath, actorId);
            Document document = Document.builder()
                    .entityType(entityType)
                    .entityId(workItem)
                    .fileName(file.getOriginalFilename())
                    .fileUrl(objectKey)
                    .status(Status.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            documentRepository.save(document);
        }
    }

    public void deleteDocuments(Long workItemId, String entityType, Long actorId) {
        List<Document> documents =
                documentRepository.findByEntityId_IdAndEntityTypeAndStatus(workItemId, entityType, Status.ACTIVE);
        for (Document document : documents) {
            deleteFromDms(document.getFileUrl(), actorId);
            document.setStatus(Status.DELETED);
            document.setUpdatedAt(LocalDateTime.now());
            documentRepository.save(document);
        }
    }

    public List<DocumentResponse> emptyDocuments() {
        return Collections.emptyList();
    }

    private String uploadToDms(MultipartFile file, String destinationPath, Long actorId) {
        try {
            var response = dmsInternalFeignClient.uploadFile(
                    file,
                    destinationPath,
                    actorId != null ? actorId : systemActorId,
                    false);
            if (response == null || response.data() == null || response.data().objectKey() == null) {
                throw new InternalErrorException("pm.document.upload.error", "DMS không trả về khóa file");
            }
            return response.data().objectKey();
        } catch (Exception e) {
            log.error("DMS upload failed for {}", destinationPath, e);
            throw new InternalErrorException("pm.document.upload.error", "Không thể upload file lên DMS");
        }
    }

    private void deleteFromDms(String key, Long actorId) {
        try {
            dmsInternalFeignClient.deleteFile(key, actorId != null ? actorId : systemActorId);
        } catch (FeignException.NotFound ex) {
            log.warn("DMS document not found when deleting key {}", key);
        } catch (Exception e) {
            log.error("DMS delete failed for key {}", key, e);
            throw new InternalErrorException("pm.document.delete.error", "Không thể xóa file trên DMS");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("pm.document.empty", "File tải lên không hợp lệ");
        }
        if (file.getSize() > 10 * 1024 * 1024L) {
            throw new BadRequestException("pm.document.size.exceeded", "Dung lượng file vượt quá 10MB");
        }
    }
}
