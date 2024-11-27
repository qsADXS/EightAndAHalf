package com.eh.user.service;

import com.eh.user.model.dto.UserDTO;
import com.eh.user.model.entity.User;
import com.eh.user.model.request.BindCodeRequest;
import com.eh.user.model.request.EmailRequest;
import com.eh.user.model.request.RegisterRequest;

import java.util.List;

public interface UserService {
    User findByUserName(String username);

    User findByEmail(String email);

    void register(RegisterRequest registerRequest);

    UserDTO getUserById(Long id);

    void updateAvatarUrl(Long id, String url);

    void bindCode(BindCodeRequest request);

    boolean sendEmailCode(EmailRequest request);

    boolean proveEmailCode(EmailRequest request);

    List<UserDTO> getUsers(List<Long> ids);
}
