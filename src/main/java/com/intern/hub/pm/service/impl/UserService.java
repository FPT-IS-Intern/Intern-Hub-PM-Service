package com.intern.hub.pm.service.impl;

import com.intern.hub.pm.dtos.response.UserResponse;
import com.intern.hub.pm.exceptions.NotFoundException;
import com.intern.hub.pm.model.User;
import com.intern.hub.pm.repository.UserRepository;
import com.intern.hub.pm.service.IUserService;
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
        String currentUsername = UserContext.requiredEmail();
        return userRepository.findAllByEmailNot(currentUsername)
                .stream()
                .map(user -> new UserResponse(user.getId(), user.getFullName(), user.getEmail()))
                .toList();
    }

    @Override
    public void verifyPin(String email, String pin) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User không tồn tại!"));
        if (user.getPinHash() == null || !passwordEncoder.matches(pin, user.getPinHash())) {
            throw new NotFoundException("Mã PIN không chính xác");
        }
    }

    @Override
    public void create(String emailUser, String pin) {
        User user = findByEmail(emailUser);
        user.setPinHash(passwordEncoder.encode(pin));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
