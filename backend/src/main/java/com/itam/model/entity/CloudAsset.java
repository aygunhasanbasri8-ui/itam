package com.itam.model.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.itam.enums.CloudProvider;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cloud_asset")
@DiscriminatorValue("CLOUD")
@PrimaryKeyJoinColumn(name = "asset_id")
@Audited
public class CloudAsset extends BaseAsset {

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "cloud_provider", nullable = false, columnDefinition = "cloud_provider")
    private CloudProvider cloudProvider;

    @Column(name = "instance_type", length = 80)
    private String instanceType;

    @Column(name = "region", length = 60)
    private String region;

    @Column(name = "account_id", length = 120)
    private String accountId;
}
