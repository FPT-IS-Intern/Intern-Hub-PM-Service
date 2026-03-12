package com.intern.hub.pm.service.impl;

import com.intern.hub.pm.dto.response.UserResponse;
import com.intern.hub.library.common.exception.NotFoundException;
import com.intern.hub.pm.model.User;
import com.intern.hub.pm.repository.UserRepository;
import com.intern.hub.pm.service.IUserService;
import com.intern.hub.pm.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;

    @Override
    public User findById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user.not.found", "Không tìm thấy user id: " + id));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("user.not.found", "Tài khoản email không tồn tại: " + email));
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public List<UserResponse> getAllUsersExceptCurrent() {
        Long currentUserId = UserContext.requiredUserId();
        return userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .map(user -> new UserResponse(user.getId(), user.getFullName(), user.getEmail()))
                .toList();
    }

}

