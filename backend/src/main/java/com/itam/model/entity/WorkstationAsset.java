package com.itam.model.entity;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
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
@Table(name = "workstation_asset")
@DiscriminatorValue("WORKSTATION")
@PrimaryKeyJoinColumn(name = "asset_id")
@Audited
public class WorkstationAsset extends BaseAsset {

    @Column(name = "serial_number", nullable = false, length = 100)
    private String serialNumber;

    @Column(name = "model", length = 150)
    private String model;

    @Column(name = "cpu", length = 150)
    private String cpu;

    @Column(name = "ram_gb")
    private Integer ramGb;

    @Column(name = "disk_gb")
    private Integer diskGb;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_person_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Person assignedPerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Department department;
}
