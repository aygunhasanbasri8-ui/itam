package tr.com.nkolay.itam.service.person.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.exception.ResourceNotFoundException;
import tr.com.nkolay.itam.mapper.PersonMapper;
import tr.com.nkolay.itam.model.dto.PageResponse;
import tr.com.nkolay.itam.model.dto.PersonRequest;
import tr.com.nkolay.itam.model.dto.PersonResponse;
import tr.com.nkolay.itam.model.entity.Department;
import tr.com.nkolay.itam.model.entity.Person;
import tr.com.nkolay.itam.repository.DepartmentRepository;
import tr.com.nkolay.itam.repository.PersonRepository;
import tr.com.nkolay.itam.repository.spec.PersonSpecifications;
import tr.com.nkolay.itam.service.person.PersonService;
import tr.com.nkolay.itam.validation.ValidationCollector;

@Service
@Transactional
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final DepartmentRepository departmentRepository;
    private final PersonMapper mapper;

    public PersonServiceImpl(PersonRepository personRepository,
            DepartmentRepository departmentRepository, PersonMapper mapper) {
        this.personRepository = personRepository;
        this.departmentRepository = departmentRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PersonResponse> list(String search, Boolean active, Long departmentId, Pageable pageable) {
        Specification<Person> spec = Specification
                .where(PersonSpecifications.nameOrCodeContains(search))
                .and(PersonSpecifications.activeEquals(active))
                .and(PersonSpecifications.departmentEquals(departmentId));

        Page<Person> page = personRepository.findAll(spec, pageable);
        return PageResponse.of(page, mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonResponse get(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    public PersonResponse create(PersonRequest request) {
        Person person = new Person();
        applyRequest(person, request);
        return mapper.toResponse(personRepository.save(person));
    }

    @Override
    public PersonResponse update(Long id, PersonRequest request) {
        Person person = findOrThrow(id);

        // FIRMA kaydı her yeni asset'in varsayılan sorumlusu; arayüzden değiştirilip
        // bozulmasın diye korunuyor. Silinemiyordu zaten, düzenlenmesi de engellendi.
        if (person.isSystem()) {
            ValidationCollector errors = ValidationCollector.create();
            errors.add("id", "sistemKaydiKorumali", "Sistem kaydı (FIRMA) düzenlenemez", id);
            errors.throwIfInvalid();
        }

        applyRequest(person, request);
        return mapper.toResponse(person);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Person findOrThrow(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kişi", id));
    }

    private void applyRequest(Person person, PersonRequest request) {
        ValidationCollector errors = ValidationCollector.create();
        Department department = resolveDepartment(request.departmentId(), errors);
        errors.throwIfInvalid();

        person.setFullName(request.fullName());
        // code ve email tekil indeksli; boş metin null'a çevrilmezse ikinci "" kaydı çakışır
        person.setCode(blankToNull(request.code()));
        person.setEmail(blankToNull(request.email()));
        person.setTitle(request.title());
        person.setPhone(request.phone());
        person.setDepartment(department);
        person.setActive(request.active());
    }

    private Department resolveDepartment(Long id, ValidationCollector errors) {
        if (id == null) {
            return null;
        }
        return departmentRepository.findByIdAndActiveTrue(id).orElseGet(() -> {
            errors.add("departmentId", "departmanBulunamadi", "Belirtilen departman bulunamadı", id);
            return null;
        });
    }
}
