package com.intern.hub.pm.feign;

import com.intern.hub.pm.config.FeignConfiguration;
import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.pm.feign.model.HrmFilterRequest;
import com.intern.hub.pm.feign.model.HrmFilterResponse;
import com.intern.hub.pm.feign.model.HrmUserClientModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "hrm",
        url = "${services.hrm.url:${HRM_SERVICE_URL:http://localhost:8081}}",
        configuration = FeignConfiguration.class
)
public interface HrmInternalFeignClient {

    @GetMapping("/hrm/internal/users/{userId}")
    ResponseApi<HrmUserClientModel> getUserByIdInternal(@PathVariable("userId") Long userId);

    @GetMapping("/hrm/internal/users/by-email")
    ResponseApi<HrmUserClientModel> getUserByEmailInternal(@RequestParam("email") String email);

    @PostMapping("/hrm/internal/users/by-ids")
    ResponseApi<List<HrmUserClientModel>> getUsersByIdsInternal(@RequestBody List<Long> userIds);

    @PostMapping("/hrm/internal/users/internal/filter")
    ResponseApi<PaginatedData<HrmFilterResponse>> filterUsers(
            @RequestBody HrmFilterRequest request,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );
}
