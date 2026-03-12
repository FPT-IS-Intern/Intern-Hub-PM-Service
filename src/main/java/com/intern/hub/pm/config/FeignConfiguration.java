package com.intern.hub.pm.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.intern.hub.pm.feign")
public class FeignConfiguration {

    @Value("${security.internal-secret}")
    private String internalSecret;

    @Bean
    public RequestInterceptor internalSecretHeaderInterceptor() {
        return requestTemplate -> requestTemplate.header("X-Internal-Secret", internalSecret);
    }
}
