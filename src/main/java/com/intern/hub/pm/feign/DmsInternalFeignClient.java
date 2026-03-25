package com.intern.hub.pm.feign;

import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.pm.feign.model.DmsDocumentClientModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "dms", url = "${services.dms.url:${DMS_SERVICE_URL:http://localhost:8084}}")
public interface DmsInternalFeignClient {

    @PostMapping(value = "/dms/internal/direct/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseApi<DmsDocumentClientModel> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam("destinationPath") String destinationPath,
            @RequestParam("actorId") Long actorId
    );

    @DeleteMapping("/dms/internal/presigned/document")
    ResponseApi<Void> deleteFile(
            @RequestParam("key") String key,
            @RequestParam("actorId") Long actorId
    );
}
