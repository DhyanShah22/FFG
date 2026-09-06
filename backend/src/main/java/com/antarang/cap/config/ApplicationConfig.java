package com.antarang.cap.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing
@EnableConfigurationProperties({JwtProperties.class, OtpProperties.class, GoogleAuthProperties.class})
public class ApplicationConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return () -> Optional.ofNullable(SecurityAuditor.getCurrentUserId());
    }
}
