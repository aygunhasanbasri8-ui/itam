package com.itam.model.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.type.SqlTypes;

import com.itam.enums.ServiceCriticality;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "business_service_asset")
@DiscriminatorValue("BUSINESS_SERVICE")
@PrimaryKeyJoinColumn(name = "asset_id")
@Audited
public class BusinessServiceAsset extends BaseAsset {

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "criticality", nullable = false, columnDefinition = "service_criticality")
    private ServiceCriticality criticality = ServiceCriticality.MID;

    @Column(name = "sla", length = 120)
    private String sla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_owner_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Person serviceOwner;
}
