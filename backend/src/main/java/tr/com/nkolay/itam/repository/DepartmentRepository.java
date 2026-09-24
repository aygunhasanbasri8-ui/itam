package tr.com.nkolay.itam.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import tr.com.nkolay.itam.model.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByIdAndActiveTrue(Long id);
}
