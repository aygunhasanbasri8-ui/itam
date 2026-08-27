package tr.com.nkolay.itam.config.audit;

import org.hibernate.envers.RevisionListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import tr.com.nkolay.itam.model.entity.ItamRevisionEntity;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Her revizyon kaydına kullanıcı ve IP bilgisini yazar.
 * <p>
 * Faz 4'te kimlik doğrulama devreye girdiğinde kullanıcı adı
 * SecurityContextHolder'dan alınacaktır; şu an sabit değer yazılıyor.
 * <p>
 * <b>Bu sınıf Spring bean DEĞİLDİR</b> — Envers tarafından örneklenir, container'dan
 * geçmez. Bu yüzden bağımlılık enjeksiyonu yerine statik erişim kullanılıyor.
 * IoC dışında kalması framework kısıtıdır, tercih değil.
 */
public class ItamRevisionListener implements RevisionListener {

	@Override
	public void newRevision(Object revisionEntity) {
		ItamRevisionEntity revision = (ItamRevisionEntity) revisionEntity;
		revision.setUsername("system");
		revision.setIpAddress(currentIp());
	}

	private String currentIp() {
		if (RequestContextHolder.getRequestAttributes()
				instanceof ServletRequestAttributes attributes) {
			HttpServletRequest request = attributes.getRequest();
			String forwarded = request.getHeader("X-Forwarded-For");
			if (forwarded != null && !forwarded.isBlank()) {
				return forwarded.split(",")[0].trim();
			}
			return request.getRemoteAddr();
		}
		return null;
	}
}
