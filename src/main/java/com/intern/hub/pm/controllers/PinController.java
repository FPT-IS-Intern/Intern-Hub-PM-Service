package com.intern.hub.pm.controllers;

import com.intern.hub.pm.dtos.request.ChangePinRequest;
import com.intern.hub.pm.dtos.request.OTPRequest;
import com.intern.hub.pm.dtos.request.PinRequest;
import com.intern.hub.pm.dtos.request.VerifyPinRequest;
import com.intern.hub.pm.dtos.response.ApiResponseBuilder;
import com.intern.hub.pm.services.PinService;
import com.intern.hub.pm.services.UserService;
import com.intern.hub.pm.utils.UserContext;
import com.intern.hub.starter.security.annotation.Authenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Pin Controller",
        description = "API liên quan đến mã pin của user."
)
@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PinController {

    private final UserService userService;
    private final PinService pinService;

    @PostMapping(path = "/verify-pin")
    @Authenticated
    @Operation(
            summary = "Xác thực mã pin",
            description = "API dùng để xác thực mã pin của user."
    )
    public ResponseEntity<?> verifyPin(
            @RequestBody VerifyPinRequest request
    ) throws Exception {
        String emailUser = UserContext.requiredEmail();

        userService.verifyPin(emailUser, request.getPin());
        return ApiResponseBuilder.success("Xác thực thành công", null);
    }

    //them xac nhan ma otp
    @PostMapping("/pin/change")
    @Authenticated
    @Operation(
            summary = "Thay đổi mã pin",
            description = "API dùng để thay đổi mã pin của user (gửi OTP xác nhận tài khoản)."
    )
    public ResponseEntity<?> changePin(
            @RequestBody @Valid ChangePinRequest request
    ) {
        String emaiUser = UserContext.requiredEmail();
        pinService.changePin(emaiUser,
                request.getOldPin(),
                request.getNewPin()
        );
        return ApiResponseBuilder.success("Đổi mã pin thành công " , null);
    }

    @PostMapping(path = "/pin")
    @Authenticated
    @Operation(
            summary = "Tạo mã pin",
            description = "API dùng để user tạo mã pin."
    )
    public ResponseEntity<?> create(
            @RequestBody PinRequest request
    ) throws Exception {
        if(!request.getPin().equals(request.getConfirmPin())){
            return ApiResponseBuilder.badRequest("Mã pin không giống nhau!");
        }
        if(request.getPin().length()<6){
            return ApiResponseBuilder.badRequest("Mã pin phải có 6 số!");
        }
        String emailUser = UserContext.requiredEmail();

        userService.create(emailUser, request.getPin());
        return ApiResponseBuilder.success("Tạo mã pin thành công", null);
    }

    //xác nhận otp
    @PostMapping(path = "/otp")
    public ResponseEntity<?> verifileOtp(
            @RequestBody OTPRequest request
    ){

        return ApiResponseBuilder.success("Tạo mã pin thành công", null);
    }
}
