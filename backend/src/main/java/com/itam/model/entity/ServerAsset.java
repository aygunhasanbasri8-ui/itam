package com.itam.model.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.type.SqlTypes;

import com.itam.enums.ServerEnvironment;

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
@Table(name = "server_asset")
@DiscriminatorValue("SERVER")
@PrimaryKeyJoinColumn(name = "asset_id")
@Audited
public class ServerAsset extends BaseAsset {

    @Column(name = "domain", length = 120)
    private String domain;

    @Column(name = "hostname", nullable = false, length = 150)
    private String hostname;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "environment", nullable = false, columnDefinition = "server_environment")
    private ServerEnvironment environment;

    @Column(name = "server_class", length = 80)
    private String serverClass;

    @Column(name = "group_name", length = 120)
    private String groupName;

    @Column(name = "group_info", length = 255)
    private String groupInfo;

    @Column(name = "group_desc", columnDefinition = "text")
    private String groupDesc;

    @Column(name = "infrastructure_type", length = 80)
    private String infrastructureType;

    @Column(name = "os", length = 120)
    private String os;

    @Column(name = "is_virtual", nullable = false)
    private boolean virtual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_owner_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Person serviceOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_responsible_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Person appResponsible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "os_responsible_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Person osResponsible;
}