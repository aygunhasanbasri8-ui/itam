package tr.com.nkolay.itam.mapper;

import org.springframework.stereotype.Component;

import tr.com.nkolay.itam.model.dto.AppUserResponse;
import tr.com.nkolay.itam.model.dto.PersonSummary;
import tr.com.nkolay.itam.model.entity.AppUser;
import tr.com.nkolay.itam.model.entity.Person;

@Component
public class AppUserMapper {

    public AppUserResponse toResponse(AppUser user) {
        return new AppUserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                toSummary(user.getPerson()),
                user.isActive(),
                user.getLastLoginAt(),
                user.getCreatedAt());
    }

    private PersonSummary toSummary(Person person) {
        return person == null ? null : new PersonSummary(person.getId(), person.getFullName());
    }
}
