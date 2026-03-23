package com.example.vocaflip.config;

import com.example.vocaflip.user.entity.User;
import com.example.vocaflip.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("test@vocaflip.com")) {
            User user = new User();
            user.setFullName("Test User");
            user.setEmail("test@vocaflip.com");
            user.setPasswordHash("$2a$10$placeholder");
            userRepository.save(user);
            System.out.println(">>> Seeded test user with id: " + user.getId());
        }
    }
}
