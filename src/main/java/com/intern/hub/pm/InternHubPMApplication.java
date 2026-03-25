package com.intern.hub.pm;

import com.intern.hub.library.common.annotation.EnableGlobalExceptionHandler;
import com.intern.hub.starter.security.annotation.EnableSecurity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
@EnableSecurity
@EnableGlobalExceptionHandler
public class InternHubPMApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(InternHubPMApplication.class);
        application.run(args);
    }

}
