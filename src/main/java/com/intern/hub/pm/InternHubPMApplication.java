package com.intern.hub.pm;

import com.intern.hub.library.common.annotation.EnableGlobalExceptionHandler;
import com.intern.hub.starter.security.annotation.EnableSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableSecurity
@EnableGlobalExceptionHandler
public class InternHubPMApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternHubPMApplication.class, args);
    }

}
