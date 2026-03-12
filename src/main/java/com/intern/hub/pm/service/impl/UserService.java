package com.intern.hub.pm.service.impl;

import com.intern.hub.pm.dto.response.UserResponse;
import com.intern.hub.pm.exceptions.NotFoundException;
import com.intern.hub.pm.model.User;
import com.intern.hub.pm.repository.UserRepository;
import com.intern.hub.pm.service.IUserService;
import com.intern.hub.pm.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy user id: " + id));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Tài khoản email không tồn tại: " + email));
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

    @Override
    public void verifyPin(Long userId, String pin) {
        User user = findById(userId);
        if (user.getPinHash() == null || !passwordEncoder.matches(pin, user.getPinHash())) {
            throw new NotFoundException("Mã PIN không chính xác");
        }
    }

    @Override
    public void create(Long userId, String pin) {
        User user = findById(userId);
        user.setPinHash(passwordEncoder.encode(pin));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}

