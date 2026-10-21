package com.itam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.itam.enums.UserRole;
import com.itam.model.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsernameAndActiveTrue(String username);

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByRoleAndActiveTrue(UserRole role);

    @EntityGraph(attributePaths = "person")
    List<AppUser> findAllByOrderByUsernameAsc();
}
