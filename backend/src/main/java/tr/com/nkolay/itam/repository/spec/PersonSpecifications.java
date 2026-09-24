package tr.com.nkolay.itam.repository.spec;

import org.springframework.data.jpa.domain.Specification;

import tr.com.nkolay.itam.model.entity.Person;

public final class PersonSpecifications {

    private PersonSpecifications() {
    }

    public static Specification<Person> nameOrCodeContains(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("fullName")), pattern),
                cb.like(cb.lower(root.get("code")), pattern),
                cb.like(cb.lower(root.get("email")), pattern));
    }

    public static Specification<Person> activeEquals(Boolean active) {
        if (active == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }

    public static Specification<Person> departmentEquals(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("department").get("id"), departmentId);
    }
}
