package tr.com.nkolay.itam.model.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import tr.com.nkolay.itam.enums.CustomFieldType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bir asset tipine özel kullanıcı tanımlı alan tanımı.
 * <p>
 * Değerlerin kendisi burada değil, ilgili asset'in base_asset.custom_fields
 * JSONB alanında field_key üzerinden anahtarlanmış olarak durur. ck_custom_field_req
 * kısıtı V1'de tüm alanları opsiyonel tutuyor; required burada bilinçli olarak
 * hep false, API'den de değiştirilemiyor.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "custom_field_def")
public class CustomFieldDefinition extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "custom_field_def_seq")
    @SequenceGenerator(name = "custom_field_def_seq", sequenceName = "custom_field_def_seq",
            allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_type_code", nullable = false)
    private AssetTypeDefinition assetType;

    @Column(name = "field_key", nullable = false, length = 60)
    private String fieldKey;

    @Column(name = "label", nullable = false, length = 120)
    private String label;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "data_type", nullable = false, columnDefinition = "custom_field_type")
    private CustomFieldType dataType;

    @Column(name = "is_required", nullable = false)
    private boolean required = false;

    @Column(name = "show_in_list", nullable = false)
    private boolean showInList = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
}
