package tr.com.nkolay.itam.service.user.impl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.enums.UserRole;
import tr.com.nkolay.itam.exception.ResourceNotFoundException;
import tr.com.nkolay.itam.mapper.AppUserMapper;
import tr.com.nkolay.itam.model.dto.AppUserRequest;
import tr.com.nkolay.itam.model.dto.AppUserResponse;
import tr.com.nkolay.itam.model.dto.AppUserUpdateRequest;
import tr.com.nkolay.itam.model.entity.AppUser;
import tr.com.nkolay.itam.model.entity.Person;
import tr.com.nkolay.itam.repository.AppUserRepository;
import tr.com.nkolay.itam.repository.PersonRepository;
import tr.com.nkolay.itam.service.user.AppUserService;
import tr.com.nkolay.itam.validation.ValidationCollector;

@Service
@Transactional
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository userRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppUserMapper mapper;

    public AppUserServiceImpl(AppUserRepository userRepository, PersonRepository personRepository,
            PasswordEncoder passwordEncoder, AppUserMapper mapper) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppUserResponse> list() {
        return userRepository.findAllByOrderByUsernameAsc().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AppUserResponse get(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    public AppUserResponse create(AppUserRequest request) {
        ValidationCollector errors = ValidationCollector.create();
        errors.check(!userRepository.existsByUsername(request.username()),
                "username", "tekil", "Bu kullanıcı adı zaten kullanılıyor", request.username());
        Person person = resolvePerson(request.personId(), errors);
        errors.throwIfInvalid();

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setPerson(person);
        return mapper.toResponse(userRepository.save(user));
    }

    @Override
    public AppUserResponse update(Long id, AppUserUpdateRequest request) {
        AppUser user = findOrThrow(id);

        ValidationCollector errors = ValidationCollector.create();
        Person person = resolvePerson(request.personId(), errors);

        // Son aktif admin pasife alınır ya da USER'a düşürülürse kimse kullanıcı
        // yönetemez hale gelir; geri dönüş ancak veritabanından olur.
        boolean losesAdmin = user.isActive() && user.getRole() == UserRole.ADMIN
                && (!request.active() || request.role() != UserRole.ADMIN);
        if (losesAdmin) {
            errors.check(userRepository.countByRoleAndActiveTrue(UserRole.ADMIN) > 1,
                    "role", "sonAdmin", "Sistemde en az bir aktif yönetici kalmalı", id);
        }
        errors.throwIfInvalid();

        user.setRole(request.role());
        user.setActive(request.active());
        user.setPerson(person);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return mapper.toResponse(user);
    }

    private Person resolvePerson(Long personId, ValidationCollector errors) {
        if (personId == null) {
            return null;
        }
        Person person = personRepository.findById(personId).orElse(null);
        errors.check(person != null, "personId", "kisiBulunamadi", "Belirtilen kişi bulunamadı", personId);
        return person;
    }

    private AppUser findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", id));
    }
}
