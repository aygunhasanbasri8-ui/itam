package tr.com.nkolay.itam.security;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import tr.com.nkolay.itam.repository.AppUserRepository;

/**
 * Repodaki varsayılan JWT anahtarı ve V7'nin seed ettiği admin/admin123 herkesin
 * elinde; üretimde bunlarla açılan bir uygulama fiilen şifresizdir. dev/test bu
 * değerlere güvendiği için kontrol yalnızca o profillerin dışında çalışır.
 * <p>
 * Kontrol bean oluşturulurken yapılır: web sunucusu henüz isteğe açılmadan
 * uygulama durur.
 */
@Component
@Profile("!dev & !test")
public class ProductionCredentialsGuard implements InitializingBean {

    // application.yml'deki varsayılanla aynı kalmalı
    private static final String DEV_JWT_SECRET =
            "gelistirme-ortami-icin-en-az-32-byte-uzunlugunda-varsayilan-anahtar";
    private static final String SEED_ADMIN_USERNAME = "admin";
    private static final String SEED_ADMIN_PASSWORD = "admin123";

    private final String jwtSecret;
    private final String adminInitialPassword;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public ProductionCredentialsGuard(@Value("${itam.jwt.secret}") String jwtSecret,
            @Value("${ADMIN_INITIAL_PASSWORD:}") String adminInitialPassword,
            AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.jwtSecret = jwtSecret;
        this.adminInitialPassword = adminInitialPassword;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void afterPropertiesSet() {
        if (jwtSecret.isBlank() || DEV_JWT_SECRET.equals(jwtSecret)) {
            throw new IllegalStateException("JWT_SECRET verilmemiş ya da geliştirme anahtarıyla aynı. "
                    + "Üretimde en az 32 byte'lık rastgele bir değer verilmeli "
                    + "(yerelde çalıştırıyorsanız dev profilini kullanın).");
        }
        replaceSeedAdminPassword();
    }

    private void replaceSeedAdminPassword() {
        appUserRepository.findByUsername(SEED_ADMIN_USERNAME)
                .filter(admin -> passwordEncoder.matches(SEED_ADMIN_PASSWORD, admin.getPasswordHash()))
                .ifPresent(admin -> {
                    // Kullanıcı ekranındaki şifre kuralıyla aynı alt sınır
                    if (adminInitialPassword.length() < 8 || SEED_ADMIN_PASSWORD.equals(adminInitialPassword)) {
                        throw new IllegalStateException("admin kullanıcısının şifresi hâlâ admin123. "
                                + "İlk açılışta ADMIN_INITIAL_PASSWORD (en az 8 karakter) verilmeli; "
                                + "şifre bununla değiştirilir, sonraki açılışlarda gerekmez.");
                    }
                    admin.setPasswordHash(passwordEncoder.encode(adminInitialPassword));
                    appUserRepository.save(admin);
                });
    }
}
