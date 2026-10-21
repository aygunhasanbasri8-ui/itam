package tr.com.nkolay.itam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import tr.com.nkolay.itam.security.JwtAuthenticationFilter;
import tr.com.nkolay.itam.security.ProblemDetailSecurityHandlers;

/**
 * UserRole.java'daki K5 kararı: iki rol yeterli. ADMIN her şeyi yapabilir,
 * USER yalnızca görüntüler/filtreler (yazma uç noktalarına dokunamaz).
 * Bu yüzden yetkilendirme burada tek noktadan, HTTP metoduna göre kuruluyor;
 * controller'lara tek tek @PreAuthorize eklemeye gerek yok.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ProblemDetailSecurityHandlers securityHandlers;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
            ProblemDetailSecurityHandlers securityHandlers) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.securityHandlers = securityHandlers;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless JWT API - CSRF token'ı olmayan istemcilere karşı korunması gereken
                // bir tarayıcı oturumu yok, bu yüzden CSRF kapalı.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Kullanıcı listesi okumada da sadece admin'e açık
                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(securityHandlers)
                        .accessDeniedHandler(securityHandlers))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
