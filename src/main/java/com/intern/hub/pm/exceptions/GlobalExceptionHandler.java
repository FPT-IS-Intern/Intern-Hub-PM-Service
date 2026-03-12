package com.intern.hub.pm.exceptions;

import com.intern.hub.library.common.dto.ResponseApi;
import com.intern.hub.library.common.dto.ResponseMetadata;
import com.intern.hub.library.common.dto.ResponseStatus;
import com.intern.hub.library.common.exception.BadRequestException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseApi<?>> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "resource.not.found", ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ResponseApi<?>> handleForbiddenException(ForbiddenException ex) {
        return build(HttpStatus.FORBIDDEN, "forbidden", ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseApi<?>> handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ResponseApi<?>> handleValidation(ValidationException ex) {
        return build(HttpStatus.BAD_REQUEST, "validation.failed", ex.getMessage());
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ResponseApi<?>> handleMultipart(Exception e) {
        return build(HttpStatus.BAD_REQUEST, "file.upload.invalid", "Lỗi upload file");
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ResponseApi<?>> handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, "conflict", ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseApi<?>> handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "unauthorized", "Email hoặc mật khẩu không đúng!");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseApi<?>> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "forbidden", "Bạn không có quyền truy cập!");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseApi<?>> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, "bad.request", ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ResponseApi<?>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String code = status == HttpStatus.UNAUTHORIZED ? "unauthorized" : "bad.request";
        return build(status, code, ex.getReason());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseApi<?>> handleException(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal.server.error", "Lỗi hệ thống " + ex.getMessage());
    }

    private ResponseEntity<ResponseApi<?>> build(HttpStatus httpStatus, String code, String message) {
        return ResponseEntity.status(httpStatus)
                .body(ResponseApi.of(new ResponseStatus(code, message), null, ResponseMetadata.fromRequestId()));
    }
}
