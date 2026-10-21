package com.itam.model.entity;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;


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
