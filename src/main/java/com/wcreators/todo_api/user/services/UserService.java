package com.wcreators.todo_api.user.services;

import com.wcreators.todo_api.constants.Roles;
import com.wcreators.todo_api.user.dto.RegistrationRequestDTO;
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

    public User saveUser(RegistrationRequestDTO registrationRequestDTO) {
        Role role = roleRepository
                .findByName(Roles.USER.getName())
                .orElseGet(() -> roleRepository.save(Role.builder().name(Roles.USER.getName()).build()));
        return userRepository.save(
                User.builder()
                        .username(registrationRequestDTO.getUsername())
                        .password(passwordEncoder.encode(registrationRequestDTO.getPassword()))
                        .role(role)
                        .build()
        );
    }

    public Optional<User> findByUserName(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByUsernameAndPassword(String username, String password) {
        Optional<User> user = findByUserName(username);
        return user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())
                ? user
                : Optional.empty();
    }
}
