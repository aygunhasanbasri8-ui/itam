package com.itam.model.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="person")
public class Person extends TimestampedEntity {


    public static final String COMPANY_CODE = "FIRMA";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "person_seq")
    @SequenceGenerator(name = "person_seq", sequenceName = "person_seq", allocationSize = 50)
    private Long id;

    @Column(name = "code", length = 40)
    private String code;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "email", length = 180)
    private String email;

    @Column(name = "title", length = 120)
    private String title;

    @Column(name = "phone", length = 30)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "is_system", nullable = false)
    private boolean system;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
