package tr.com.nkolay.itam.service.person;

import org.springframework.data.domain.Pageable;

import tr.com.nkolay.itam.model.dto.PageResponse;
import tr.com.nkolay.itam.model.dto.PersonRequest;
import tr.com.nkolay.itam.model.dto.PersonResponse;

public interface PersonService {

    PageResponse<PersonResponse> list(String search, Boolean active, Long departmentId, Pageable pageable);

    PersonResponse get(Long id);

    PersonResponse create(PersonRequest request);

    PersonResponse update(Long id, PersonRequest request);
}
