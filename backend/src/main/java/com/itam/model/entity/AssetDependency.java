package com.itam.model.entity;

import java.time.OffsetDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.itam.enums.DependencyType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
 * İki asset arasındaki bağımlılık kenarı (örn. iş servisi -> uygulama -> sunucu).
 * <p>
 * base_asset üzerinde is_deleted/updated_at gibi alanlar taşımadığı için
 * TimestampedEntity/AuditableEntity'den türetilmiyor; tablo yalnızca
 * created_at/created_by tutuyor (kenar oluşturulduktan sonra değişmiyor,
 * güncelleme yerine silinip yeniden eklenir).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "asset_dependency")
@EntityListeners(AuditingEntityListener.class)
public class AssetDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "asset_dependency_seq")
    @SequenceGenerator(name = "asset_dependency_seq", sequenceName = "asset_dependency_seq",
            allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_asset_id", nullable = false)
    private BaseAsset parentAsset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_asset_id", nullable = false)
    private BaseAsset childAsset;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "dependency_type", nullable = false, columnDefinition = "dependency_type")
    private DependencyType dependencyType = DependencyType.RUNS_ON;

    @Column(name = "notes", length = 255)
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 80)
    private String createdBy;
}
