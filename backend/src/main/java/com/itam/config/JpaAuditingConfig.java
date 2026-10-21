package com.itam.config;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.itam.security.CurrentUser;

/** created_by / updated_by alanlarını oturumdaki kullanıcıyla dolduran yapılandırma. */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "auditDateTimeProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of(CurrentUser.usernameOrSystem());
    }

    @Bean
    public DateTimeProvider auditDateTimeProvider() {
        return new DateTimeProvider() {

            @Override
            public Optional<TemporalAccessor> getNow() {
                return Optional.of(OffsetDateTime.now());
            }
        };
    }
}