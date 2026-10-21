package com.itam.model.entity;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.type.SqlTypes;

import com.itam.enums.AssetStatus;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "base_asset")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "asset_kind", discriminatorType = DiscriminatorType.STRING)
@Audited
public abstract class BaseAsset extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "base_asset_seq")
    @SequenceGenerator(name = "base_asset_seq", sequenceName = "base_asset_seq",
            allocationSize = 50)
    private Long id;

    @Column(name = "asset_name", nullable = false, length = 200)
    private String assetName;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_type", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private AssetTypeDefinition assetType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsibility_owner_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Person responsibilityOwner;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "asset_status")
    private AssetStatus status = AssetStatus.ACTIVE;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    ///hizmet dışı ama envanterde
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    /** field_key -> değer; hangi anahtarların geçerli olduğu custom_field_def'ten gelir. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_fields", nullable = false, columnDefinition = "jsonb")
    private Map<String, String> customFields = new HashMap<>();
}