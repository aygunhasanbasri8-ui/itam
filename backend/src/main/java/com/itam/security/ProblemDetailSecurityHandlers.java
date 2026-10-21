package com.itam.security;

import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Spring Security'nin varsayılan 401/403 gövdesi (boş veya düz metin)
 * GlobalExceptionHandler'ın ürettiği RFC 7807 formatından farklı olurdu;
 * istemci tarafı tek bir hata gösterim bileşeni kullanabilsin diye burada
 * aynı şekle (ProblemDetail) çevriliyor.
 */
@Component
public class ProblemDetailSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final String BASE_URI = "https://itam.example.com/errors/";

    private final ObjectMapper objectMapper;

    public ProblemDetailSecurityHandlers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        write(response, HttpStatus.UNAUTHORIZED, "unauthorized",
                "Kimlik doğrulama gerekli", "Geçerli bir Bearer token ile istek atın.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        write(response, HttpStatus.FORBIDDEN, "forbidden",
                "Bu işlem için yetkiniz yok", "Bu uç nokta mevcut rolünüze kapalı.");
    }

    private void write(HttpServletResponse response, HttpStatus status, String type,
            String title, String detail) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setType(URI.create(BASE_URI + type));
        pd.setTitle(title);
        pd.setDetail(detail);
        pd.setProperty("timestamp", OffsetDateTime.now());

        response.setStatus(status.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), pd);
    }
}
