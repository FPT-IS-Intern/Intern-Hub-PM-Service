package com.intern.hub.pm.controller;

import com.intern.hub.library.common.dto.PaginatedData;
import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.pm.feign.HrmInternalFeignClient;
import com.intern.hub.pm.feign.model.HrmFilterRequest;
import com.intern.hub.pm.feign.model.HrmFilterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/users")
@Tag(name = "Filter user", description = "Các API liên quan đến user")
@SecurityRequirement(name = "Bearer")
public class UserfiterController {

    private final HrmInternalFeignClient hrmInternalFeignClient;

    @PostMapping("/filter")
    @Operation(summary = "Lọc danh sách user từ HRM", description = "Gọi sang HRM-Service để lấy danh sách user theo bộ lọc.")
    public ResponseApi<PaginatedData<HrmFilterResponse>> filterUsers(
            @RequestBody HrmFilterRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (request.getSysStatuses() != null && request.getSysStatuses().isEmpty()) {
            request.setSysStatuses(null);
        }
        if (request.getRoles() != null && request.getRoles().isEmpty()) {
            request.setRoles(null);
        }
        if (request.getPositions() != null && request.getPositions().isEmpty()) {
            request.setPositions(null);
        }

        return hrmInternalFeignClient.filterUsers(request, page, size);
    }
}
