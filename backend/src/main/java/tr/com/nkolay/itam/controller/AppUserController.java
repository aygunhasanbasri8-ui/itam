package tr.com.nkolay.itam.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tr.com.nkolay.itam.model.dto.AppUserRequest;
import tr.com.nkolay.itam.model.dto.AppUserResponse;
import tr.com.nkolay.itam.model.dto.AppUserUpdateRequest;
import tr.com.nkolay.itam.service.user.AppUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class AppUserController {

    private final AppUserService userService;

    public AppUserController(AppUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<AppUserResponse> list() {
        return userService.list();
    }

    @GetMapping("/{id}")
    public AppUserResponse get(@PathVariable Long id) {
        return userService.get(id);
    }

    @PostMapping
    public ResponseEntity<AppUserResponse> create(@Valid @RequestBody AppUserRequest request) {
        AppUserResponse created = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public AppUserResponse update(@PathVariable Long id, @Valid @RequestBody AppUserUpdateRequest request) {
        return userService.update(id, request);
    }
}
