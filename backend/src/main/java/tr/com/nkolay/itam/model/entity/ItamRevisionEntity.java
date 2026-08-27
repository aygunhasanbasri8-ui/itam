package tr.com.nkolay.itam.model.entity;

import jakarta.persistence.*;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import tr.com.nkolay.itam.config.audit.ItamRevisionListener;

import lombok.Getter;
import lombok.Setter;

/**
 * Envers'in standart revizyon tablosunu genişletir.
 * <p>
 * Varsayılan yapı yalnızca revizyon numarası ve zaman damgası tutar;
 * "kimin, ne zaman, hangi alanı değiştirdiği" gereksinimi için kullanıcı adı
 * ve IP adresi eklendi.
 * <p>
 * revinfo tablosu V1 migration'da tanımlıdır.
 */
@Getter
@Setter
@Entity
@Table(name = "revinfo")
@RevisionEntity(ItamRevisionListener.class)
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "rev", nullable = false)),
        @AttributeOverride(name = "timestamp", column = @Column(name = "revtstmp"))
})
public class ItamRevisionEntity extends DefaultRevisionEntity {

	@Column(name = "username", length = 80)
	private String username;

	@Column(name = "ip_addr", length = 45)
	private String ipAddress;
}
