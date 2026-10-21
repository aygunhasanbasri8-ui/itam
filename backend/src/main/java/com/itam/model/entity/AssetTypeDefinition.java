package com.itam.model.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.itam.enums.AssetKind;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "asset_type_def")
public class AssetTypeDefinition extends TimestampedEntity {

	@Id
	@Column(name = "code", length = 40)
	private String code;

	@Column(name = "display_name", nullable = false, length = 100)
	private String displayName;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "kind", nullable = false, columnDefinition = "asset_kind")
	private AssetKind kind;

	@Column(name = "icon", nullable = false, length = 60)
	private String icon;

	@Column(name = "description", length = 255)
	private String description;

	@Column(name = "is_system", nullable = false)
	private boolean system;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;
}
