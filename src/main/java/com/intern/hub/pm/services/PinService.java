package com.intern.hub.pm.services;

import com.intern.hub.pm.exceptions.NotFoundException;
import com.intern.hub.pm.models.User;
import com.intern.hub.pm.repositorys.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PinService {
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final int MAX_CHANGE_FAIL = 3;
    private static final Duration CHANGE_LOCK_TIME = Duration.ofMinutes(10);

    private static final int MAX_FAIL = 5;
    private static final Duration LOCK_TIME = Duration.ofMinutes(5);

    public void verifyPin(Long userId, String inputPin) {

        String lockKey = "pin:lock:" + userId;

        // Kiểm tra đang bị khóa không
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new RuntimeException("PIN đang bị khóa, thử lại sau");
        }

        User user = userRepository.findById(userId)
                .orElseThrow();

        //Nếu sai
        if (!passwordEncoder.matches(inputPin, user.getPinHash())) {

            String failKey = "pin:fail:" + userId;

            Long failCount = redisTemplate.opsForValue().increment(failKey);

            // Set TTL nếu lần đầu
            if (failCount != null && failCount == 1) {
                redisTemplate.expire(failKey, LOCK_TIME);
            }

            if (failCount != null && failCount >= MAX_FAIL) {
                redisTemplate.opsForValue().set(lockKey, "LOCK", LOCK_TIME);
            }

            throw new RuntimeException("Sai mã PIN");
        }

        redisTemplate.delete("pin:fail:" + userId);
    }



    public void changePin(String emailUser, String oldPin, String newPin) {

        User user = userRepository.findByEmail(emailUser)
                .orElseThrow(()-> new NotFoundException("Không tìm thấy user có email: "+ emailUser));


        String lockKey = "change:lock:" + user.getId();

        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new NotFoundException("Chức năng đổi PIN bị khóa");
        }

        if (!passwordEncoder.matches(oldPin, user.getPinHash())) {

            String failKey = "change:fail:" + user.getId();

            Long failCount = redisTemplate.opsForValue().increment(failKey);

            if (failCount != null && failCount == 1) {
                redisTemplate.expire(failKey, CHANGE_LOCK_TIME);
            }

            if (failCount != null && failCount >= MAX_CHANGE_FAIL) {
                redisTemplate.opsForValue().set(lockKey, "LOCK", CHANGE_LOCK_TIME);
            }

            throw new NotFoundException("Sai PIN cũ");
        }

        user.setPinHash(passwordEncoder.encode(newPin));
        userRepository.save(user);

        redisTemplate.delete("change:fail:" + user.getId());
    }
}
