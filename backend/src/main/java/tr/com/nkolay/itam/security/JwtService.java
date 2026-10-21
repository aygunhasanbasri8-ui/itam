package tr.com.nkolay.itam.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Token üretimi ve doğrulaması burada toplanır; filter ve login akışı bunu kullanır. */
@Component
public class JwtService {

    private static final String ROLE_CLAIM = "role";

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(@Value("${itam.jwt.secret}") String secret,
            @Value("${itam.jwt.expiration-minutes}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim(ROLE_CLAIM, role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    /** Geçersiz/süresi geçmiş token'da jjwt kendi RuntimeException alt sınıflarını fırlatır, burada yutulmuyor. */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
