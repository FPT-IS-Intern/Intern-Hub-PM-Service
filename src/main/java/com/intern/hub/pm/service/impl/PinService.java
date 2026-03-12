package com.intern.hub.pm.service.impl;

import com.intern.hub.pm.exceptions.NotFoundException;
import com.intern.hub.pm.model.User;
import com.intern.hub.pm.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PinService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    public void changePin(Long userId, String oldPin, String newPin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User không tồn tại!"));

        if (user.getPinHash() == null || !passwordEncoder.matches(oldPin, user.getPinHash())) {
            throw new IllegalArgumentException("Mã PIN hiện tại không chính xác");
        }
        if (newPin == null || newPin.length() != 6) {
            throw new IllegalArgumentException("Mã PIN mới phải có đúng 6 số");
        }

        user.setPinHash(passwordEncoder.encode(newPin));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
