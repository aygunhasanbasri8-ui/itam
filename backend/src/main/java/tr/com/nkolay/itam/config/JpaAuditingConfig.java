package tr.com.nkolay.itam.config;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * created_by / updated_by alanlarını dolduran yapılandırma.
 * <p>
 * Faz 4'te kimlik doğrulama devreye girdiğinde auditorAware,
 * SecurityContextHolder üzerinden oturum açan kullanıcıyı dönecek şekilde
 * değiştirilecektir. Şu an sabit değer dönüyor.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "auditDateTimeProvider")
public class JpaAuditingConfig {

    public static final String SYSTEM_USER = "system";

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of(SYSTEM_USER);
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