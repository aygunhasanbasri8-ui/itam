package tr.com.nkolay.itam.repository.spec;

import org.springframework.data.jpa.domain.Specification;

import tr.com.nkolay.itam.model.entity.Department;

public final class DepartmentSpecifications {

    private DepartmentSpecifications() {
    }

    public static Specification<Department> activeEquals(Boolean active) {
        if (active == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }

    public static Specification<Department> nameOrCodeContains(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("code")), pattern));
    }
}
