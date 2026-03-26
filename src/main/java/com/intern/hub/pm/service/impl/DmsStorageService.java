package com.intern.hub.pm.service.impl;

import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.pm.feign.DmsInternalFeignClient;
import com.intern.hub.pm.feign.model.DmsDocumentClientModel;
import com.intern.hub.pm.repository.FileStorageRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DmsStorageService implements FileStorageRepository {

    private final DmsInternalFeignClient dmsInternalFeignClient;

    @Value("${services.dms.system-actor-id:0}")
    private Long systemActorId;

    @Override
    public String uploadFile(MultipartFile file, String keyPrefix, Long actorId) {
        return uploadFile(file, keyPrefix, actorId, 20 * 1024 * 1024L, ".*");
    }

    @Override
    public String uploadFile(
            MultipartFile file, String keyPrefix, Long actorId,
            Long maxSizeBytes, String contentTypeRegex
    ) {
        if (file.getSize() > maxSizeBytes) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dung lượng file vượt quá giới hạn " + (maxSizeBytes / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.matches(contentTypeRegex)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Định dạng file không hợp lệ. Yêu cầu: " + contentTypeRegex);
        }

        try {
            Long requestActorId = actorId != null ? actorId : systemActorId;
            ResponseApi<DmsDocumentClientModel> response =
                    dmsInternalFeignClient.uploadFile(file, keyPrefix, requestActorId, false);

            if (response == null || response.data() == null || !hasText(response.data().objectKey())) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "DMS không trả về thông tin file sau khi upload");
            }

            return response.data().objectKey();
        } catch (FeignException e) {
            log.error("DMS upload failed with status {} and message: {}", e.status(), e.contentUTF8(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi từ DMS: " + e.status() + " - " + e.getMessage());
        } catch (Exception e) {
            log.error("DMS upload failed for destination path {}", keyPrefix, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Không thể upload file lên hệ thống lưu trữ: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String key, Long actorId) {
        try {
            dmsInternalFeignClient.deleteFile(key, actorId != null ? actorId : systemActorId);
        } catch (FeignException.NotFound ex) {
            log.warn("DMS document not found when deleting key {}", key);
        } catch (Exception e) {
            log.error("DMS delete failed for key {}", key, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Không thể delete file trong hệ thống lưu trữ");
        }
    }

    @Override
    public String getPrivateUrl(String key) {
        return key;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
