package com.itam.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itam.model.dto.PageResponse;
import com.itam.model.dto.PersonRequest;
import com.itam.model.dto.PersonResponse;
import com.itam.service.person.PersonService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/people")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public PageResponse<PersonResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long departmentId,
            @PageableDefault(size = 20, sort = "fullName") Pageable pageable) {
        return personService.list(search, active, departmentId, pageable);
    }

    @GetMapping("/{id}")
    public PersonResponse get(@PathVariable Long id) {
        return personService.get(id);
    }

    @PostMapping
    public ResponseEntity<PersonResponse> create(@Valid @RequestBody PersonRequest request) {
        PersonResponse created = personService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public PersonResponse update(@PathVariable Long id, @Valid @RequestBody PersonRequest request) {
        return personService.update(id, request);
    }
}
