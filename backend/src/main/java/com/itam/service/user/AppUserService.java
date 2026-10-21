package com.itam.service.user;

import java.util.List;

import com.itam.model.dto.AppUserRequest;
import com.itam.model.dto.AppUserResponse;
import com.itam.model.dto.AppUserUpdateRequest;

public interface AppUserService {

    List<AppUserResponse> list();

    AppUserResponse get(Long id);

    AppUserResponse create(AppUserRequest request);

    AppUserResponse update(Long id, AppUserUpdateRequest request);
}
