package com.intern.hub.pm.controller;

import com.intern.hub.pm.dtos.response.ApiResponseBuilder;
import com.intern.hub.pm.service.impl.UserService;
import com.intern.hub.starter.security.annotation.Authenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "User Controller",
        description = "API liên quan đến user."
)
@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @GetMapping(path = "/users/lists")
    @Authenticated
    @Operation(
            summary = "Danh sách user",
            description = "API dùng để lấy danh sách user (không có tài khoản user đang login, load list user để thêm vào dự án)."
    )
    public ResponseEntity<?> getUsers() {
        return ApiResponseBuilder.success("Danh sách user không có user ", userService.getAllUsersExceptCurrent());
    }
}
