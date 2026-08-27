package tr.com.nkolay.itam.model.entity;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Zaman damgalarına ek olarak işlemi yapan kullanıcıyı da tutan üst sınıf.
 * Şu an yalnızca base_asset kullanıyor.
 * <p>
 * Değerler JpaAuditingConfig içindeki AuditorAware'den gelir; Faz 4'te
 * SecurityContext'e bağlanacaktır.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AuditableEntity extends TimestampedEntity {

	@CreatedBy
	@Column(name = "created_by", updatable = false, length = 80)
	private String createdBy;

	@LastModifiedBy
	@Column(name = "updated_by", length = 80)
	private String updatedBy;
}
