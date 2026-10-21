package com.itam.model.entity;

import jakarta.persistence.*;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import com.itam.config.audit.ItamRevisionListener;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "revinfo")
@RevisionEntity(ItamRevisionListener.class)
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "rev", nullable = false)),
        @AttributeOverride(name = "timestamp", column = @Column(name = "revtstmp"))
})
public class ItamRevisionEntity extends DefaultRevisionEntity {

	@Column(name = "username", length = 80)
	private String username;

	@Column(name = "ip_addr", length = 45)
	private String ipAddress;
}
