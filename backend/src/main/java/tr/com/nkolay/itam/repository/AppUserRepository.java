package tr.com.nkolay.itam.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import tr.com.nkolay.itam.model.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsernameAndActiveTrue(String username);

    boolean existsByUsername(String username);
}
