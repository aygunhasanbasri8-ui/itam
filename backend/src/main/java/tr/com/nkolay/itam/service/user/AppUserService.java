package tr.com.nkolay.itam.service.user;

import java.util.List;

import tr.com.nkolay.itam.model.dto.AppUserRequest;
import tr.com.nkolay.itam.model.dto.AppUserResponse;
import tr.com.nkolay.itam.model.dto.AppUserUpdateRequest;

public interface AppUserService {

    List<AppUserResponse> list();

    AppUserResponse get(Long id);

    AppUserResponse create(AppUserRequest request);

    AppUserResponse update(Long id, AppUserUpdateRequest request);
}
