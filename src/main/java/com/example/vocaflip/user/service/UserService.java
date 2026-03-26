package com.example.vocaflip.user.service;

import com.example.vocaflip.user.dto.request.UpdateProfileRequest;
import com.example.vocaflip.user.dto.response.UserResponse;
import com.example.vocaflip.user.entity.User;
import com.example.vocaflip.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getCurrentUser(String email) {
        User user = findByEmail(email);
        return UserResponse.from(user);
    }

    public UserResponse updateCurrentUser(String email, UpdateProfileRequest request) {
        User user = findByEmail(email);

        if (request.fullName() != null) {
            String fullName = request.fullName().trim();

            if (fullName.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name must not be blank");
            }

            user.setFullName(fullName);
        }

        if (request.dailyGoal() != null) {
            user.setDailyGoal(request.dailyGoal());
        }

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
