package tr.com.nkolay.itam.security;

import java.time.OffsetDateTime;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.model.dto.LoginResponse;
import tr.com.nkolay.itam.model.entity.AppUser;
import tr.com.nkolay.itam.repository.AppUserRepository;

@Service
@Transactional
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(String username, String password) {
        AppUser user = appUserRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new BadCredentialsException("Kullanıcı adı veya şifre hatalı"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Kullanıcı adı veya şifre hatalı");
        }

        user.setLastLoginAt(OffsetDateTime.now());

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return new LoginResponse(token, "Bearer", user.getUsername(), user.getRole().name());
    }
}
