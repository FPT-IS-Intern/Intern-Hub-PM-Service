package com.intern.hub.pm.controller.internal;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.pm.feign.HrmInternalFeignClient;
import com.intern.hub.pm.feign.model.HrmUserClientModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/internal")
@RequiredArgsConstructor
@SecurityRequirement(name = "InternalAPIKey")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalUserController {

    private final HrmInternalFeignClient hrmInternalFeignClient;

    @GetMapping("/users/by-email")
    public ResponseApi<HrmUserClientModel> getUserByEmail(@RequestParam("email") String email) {
        return hrmInternalFeignClient.getUserByEmailInternal(email);
    }

    @GetMapping("/users/search")
    public ResponseApi<java.util.List<HrmUserClientModel>> searchUsers(@RequestParam("keyword") String keyword) {
        return hrmInternalFeignClient.searchUsersInternal(keyword);
    }
}
