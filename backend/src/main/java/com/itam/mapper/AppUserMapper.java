package com.itam.mapper;

import org.springframework.stereotype.Component;

import com.itam.model.dto.AppUserResponse;
import com.itam.model.dto.PersonSummary;
import com.itam.model.entity.AppUser;
import com.itam.model.entity.Person;

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
