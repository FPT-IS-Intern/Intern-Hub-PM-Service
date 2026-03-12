package com.intern.hub.pm.feign;

import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.pm.feign.model.HrmUserClientModel;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "hrm", url = "${services.hrm.url:${HRM_SERVICE_URL:http://localhost:8081}}")
public interface HrmInternalFeignClient {

    @GetMapping("/hrm/internal/users/{userId}")
    ResponseApi<HrmUserClientModel> getUserByIdInternal(@PathVariable("userId") Long userId);

    @PostMapping("/hrm/internal/users/by-ids")
    ResponseApi<List<HrmUserClientModel>> getUsersByIdsInternal(@RequestBody List<Long> userIds);
}
