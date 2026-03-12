package com.intern.hub.pm;

import com.intern.hub.library.common.annotation.EnableGlobalExceptionHandler;
import com.intern.hub.starter.security.annotation.EnableSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableSecurity
@EnableGlobalExceptionHandler
@EnableScheduling
public class InternHubPMApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternHubPMApplication.class, args);
    }

}
