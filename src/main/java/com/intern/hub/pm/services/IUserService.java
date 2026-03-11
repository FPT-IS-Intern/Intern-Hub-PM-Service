package com.intern.hub.pm.services;

import com.intern.hub.pm.dtos.response.UserResponse;
import com.intern.hub.pm.models.User;

import java.util.List;

public interface IUserService {

    User findById(long id);
    User findByEmail(String email);

    boolean existsById(Long id);

    List<UserResponse> getAllUsersExceptCurrent();

    void verifyPin(String email, String pin) throws Exception;

    void create(String emaiUser, String pin) throws Exception;
}
