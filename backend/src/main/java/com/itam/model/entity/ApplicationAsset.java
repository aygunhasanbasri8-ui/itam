package com.itam.model.entity;

import java.time.LocalDate;

import org.hibernate.envers.Audited;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "application_asset")
@DiscriminatorValue("APPLICATION")
@PrimaryKeyJoinColumn(name = "asset_id")
@Audited
public class ApplicationAsset extends BaseAsset {

    @Column(name = "version", length = 60)
    private String version;

    @Column(name = "license_type", length = 80)
    private String licenseType;

    @Column(name = "license_count")
    private Integer licenseCount;

    @Column(name = "vendor", length = 150)
    private String vendor;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "install_path", length = 400)
    private String installPath;
}
