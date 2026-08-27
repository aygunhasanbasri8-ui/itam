package tr.com.nkolay.itam.model.entity;

import java.time.OffsetDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * created_at ve updated_at kolonlarını taşıyan entity'ler için üst sınıf.
 * <p>
 * department, person, app_user, asset_type_def ve custom_field_def bunu kullanır.
 * <p>
 * <b>Neden iki ayrı üst sınıf:</b> created_by / updated_by kolonları yalnızca
 * base_asset tablosunda vardır. Tek bir üst sınıf kullanılsaydı ddl-auto: validate
 * diğer tablolarda eksik kolon hatası verir ve uygulama açılmazdı.
 * <p>
 * MappedSuperclass olduğu için ortada "timestamped" diye bir tablo yoktur;
 * yalnızca alanlar miras verilir.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class TimestampedEntity {

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;
}
