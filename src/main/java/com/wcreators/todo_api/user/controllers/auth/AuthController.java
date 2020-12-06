package com.wcreators.todo_api.user.controllers.auth;

import com.wcreators.todo_api.configs.security.jwt.JwtProvider;
import com.wcreators.todo_api.user.dto.AuthRequestDTO;
import com.wcreators.todo_api.user.dto.AuthResponseDTO;
import com.wcreators.todo_api.user.dto.RegistrationRequestDTO;
import com.wcreators.todo_api.user.entities.User;
import com.wcreators.todo_api.user.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    public String signup(@RequestBody @Valid RegistrationRequestDTO body) {
        User user = User.builder()
                .username(body.getUsername())
                .password(body.getPassword())
                .build();
        userService.saveUser(user);
        return "OK";
    }

    @PostMapping("/signin")
    public AuthResponseDTO signin(@RequestBody @Valid AuthRequestDTO body) {
        User user = userService.findByUsernameAndPassword(body.getUsername(), body.getPassword());
        String token = jwtProvider.generateToken(user.getUsername());
        return AuthResponseDTO.builder().token(token).build();
    }
}
