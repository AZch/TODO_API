package com.wcreators.todo_api.user.services;

import com.wcreators.todo_api.user.entities.Role;
import com.wcreators.todo_api.user.entities.User;
import com.wcreators.todo_api.user.repositories.RoleRepository;
import com.wcreators.todo_api.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // TODO use model for user sign up here
    public User saveUser(User user) {
        Role role = roleRepository
                .findByName("USER")
                .orElseThrow(
//                        roleRepository.save(
//                                Role.builder().name("USER").build()
//                        )
                );

        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByUserName(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByUsernameAndPassword(String username, String password) {
        Optional<User> user = findByUserName(username);
        return user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())
                ? user.get()
                : null;
    }
}
