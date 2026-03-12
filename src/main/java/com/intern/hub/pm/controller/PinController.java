package com.intern.hub.pm.controller;

import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.library.common.exception.BadRequestException;
import com.intern.hub.pm.dto.request.ChangePinRequest;
import com.intern.hub.pm.dto.request.OTPRequest;
import com.intern.hub.pm.dto.request.PinRequest;
import com.intern.hub.pm.dto.request.VerifyPinRequest;
import com.intern.hub.pm.service.impl.PinService;
import com.intern.hub.pm.service.impl.UserService;
import com.intern.hub.pm.utils.UserContext;
import com.intern.hub.starter.security.annotation.Authenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Pin Controller",
        description = "API liên quan đến mã pin của user."
)
@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PinController {

    UserService userService;
    PinService pinService;

    @PostMapping(path = "/verify-pin")
    @Authenticated
    @Operation(
            summary = "Xác thực mã pin",
            description = "API dùng để xác thực mã pin của user."
    )
    public ResponseApi<?> verifyPin(
            @RequestBody VerifyPinRequest request
    ) {
        Long userId = UserContext.requiredUserId();
        userService.verifyPin(userId, request.getPin());
        return ResponseApi.noContent();
    }

    //them xac nhan ma otp
    @PostMapping("/pin/change")
    @Authenticated
    @Operation(
            summary = "Thay đổi mã pin",
            description = "API dùng để thay đổi mã pin của user (gửi OTP xác nhận tài khoản)."
    )
    public ResponseApi<?> changePin(
            @RequestBody @Valid ChangePinRequest request
    ) {
        Long userId = UserContext.requiredUserId();
        pinService.changePin(userId,
                request.getOldPin(),
                request.getNewPin()
        );
        return ResponseApi.noContent();
    }

    @PostMapping(path = "/pin")
    @Authenticated
    @Operation(
            summary = "Tạo mã pin",
            description = "API dùng để user tạo mã pin."
    )
    public ResponseApi<?> create(
            @RequestBody PinRequest request
    ) {
        if (!request.getPin().equals(request.getConfirmPin())) {
            throw new BadRequestException("pin.confirm.invalid", "Mã pin không giống nhau!");
        }
        if (request.getPin().length() < 6) {
            throw new BadRequestException("pin.invalid", "Mã pin phải có 6 số!");
        }
        Long userId = UserContext.requiredUserId();
        userService.create(userId, request.getPin());
        return ResponseApi.noContent();
    }

    //xác nhận otp
    @PostMapping(path = "/otp")
    public ResponseApi<?> verifileOtp(
            @RequestBody OTPRequest request
    ) {

        return ResponseApi.noContent();
    }
}

