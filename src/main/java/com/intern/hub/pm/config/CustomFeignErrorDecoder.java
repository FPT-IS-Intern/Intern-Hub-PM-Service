package com.intern.hub.pm.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.hub.library.common.exception.ConflictDataException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class CustomFeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() >= 400 && response.status() <= 500) {
            try {
                if (response.body() != null) {
                    String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                    log.debug("[CustomFeignErrorDecoder] Error body from external service: {}", body);

                    JsonNode jsonNode = objectMapper.readTree(body);

                    // Nếu là một mảng, lấy phần tử đầu tiên (Wallet Service đôi khi trả về mảng
                    // lỗi)
                    if (jsonNode.isArray() && !jsonNode.isEmpty()) {
                        jsonNode = jsonNode.get(0);
                    }

                    // Trường hợp 1: Cấu trúc lồng nhau của PM/HRM (status: { message: "..." })
                    if (jsonNode.has("status") && jsonNode.get("status").isObject()
                            && jsonNode.get("status").has("message")) {
                        String message = jsonNode.get("status").get("message").asText();
                        return new ConflictDataException(message);
                    }

                    // Trường hợp 2: Cấu trúc phẳng của Wallet (message: "...")
                    if (jsonNode.has("message")) {
                        String message = jsonNode.get("message").asText();
                        return new ConflictDataException(message);
                    }
                }
            } catch (Exception e) {
                log.error("[CustomFeignErrorDecoder] Error while decoding Feign error body", e);
            }
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }
}
