package com.itam.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.itam.model.entity.Department;

public interface DepartmentRepository
        extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    Optional<Department> findByIdAndActiveTrue(Long id);
}
