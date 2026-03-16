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

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableSecurity
@EnableGlobalExceptionHandler
public class InternHubPMApplication {
    private static final Logger log = LoggerFactory.getLogger(InternHubPMApplication.class);

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(InternHubPMApplication.class);
        application.addListeners((ApplicationEnvironmentPreparedEvent event) -> logSecurityConfig(event.getEnvironment()));
        application.run(args);
    }

    private static void logSecurityConfig(ConfigurableEnvironment environment) {
        String envInternalSecretKey = System.getenv("INTERNAL_SECRET_KEY");
        String envInternalSecret = System.getenv("INTERNAL_SECRET");
        String propertyInternalSecret = environment.getProperty("security.internal-secret");
        String envDatasourceUrl = System.getenv("SPRING_DATASOURCE_URL");
        String envDatasourceUsername = System.getenv("SPRING_DATASOURCE_USERNAME");
        String propertyDatasourceUrl = environment.getProperty("spring.datasource.url");
        String propertyDatasourceUsername = environment.getProperty("spring.datasource.username");

        List<String> matchingSources = new ArrayList<>();
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource<?> enumerablePropertySource) {
                for (String propertyName : enumerablePropertySource.getPropertyNames()) {
                    if ("security.internal-secret".equals(propertyName)
                            || "INTERNAL_SECRET_KEY".equals(propertyName)
                            || "INTERNAL_SECRET".equals(propertyName)
                            || "spring.datasource.url".equals(propertyName)
                            || "spring.datasource.username".equals(propertyName)
                            || "SPRING_DATASOURCE_URL".equals(propertyName)
                            || "SPRING_DATASOURCE_USERNAME".equals(propertyName)) {
                        matchingSources.add(propertySource.getName() + "=" + mask(enumerablePropertySource.getProperty(propertyName)));
                    }
                }
            }
        }

        log.info("Startup config check: INTERNAL_SECRET_KEY={}, INTERNAL_SECRET={}, security.internal-secret={}, SPRING_DATASOURCE_URL={}, SPRING_DATASOURCE_USERNAME={}, spring.datasource.url={}, spring.datasource.username={}, sources={}",
                mask(envInternalSecretKey),
                mask(envInternalSecret),
                mask(propertyInternalSecret),
                mask(envDatasourceUrl),
                mask(envDatasourceUsername),
                mask(propertyDatasourceUrl),
                mask(propertyDatasourceUsername),
                matchingSources.isEmpty() ? "[]" : matchingSources);
    }

    private static String mask(Object value) {
        if (value == null) {
            return "<null>";
        }

        String text = String.valueOf(value);
        if (text.isBlank()) {
            return "<blank>";
        }
        if (text.length() <= 4) {
            return "****";
        }
        if ("null".equalsIgnoreCase(text)) {
            return "<literal:null>";
        }
        return text.substring(0, 4) + "..." + text.substring(text.length() - 2);
    }

}
