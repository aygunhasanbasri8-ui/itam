package tr.com.nkolay.itam.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import tr.com.nkolay.itam.model.entity.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findByCode(String code);

    Optional<Person> findByIdAndActiveTrue(Long id);
}