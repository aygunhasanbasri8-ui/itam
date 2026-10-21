package tr.com.nkolay.itam.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Denetim alanlarına yazılacak kullanıcı adı.
 * <p>
 * Oturum yoksa (Flyway seed'i, zamanlanmış iş, login öncesi istek) "system"
 * döner; created_by NOT NULL olduğu için boş bırakmak kaydı patlatırdı.
 */
public final class CurrentUser {

    public static final String SYSTEM_USER = "system";

    private CurrentUser() {
    }

    public static String usernameOrSystem() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return SYSTEM_USER;
        }
        return authentication.getName();
    }
}
