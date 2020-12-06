package com.wcreators.todo_api.user.controllers.auth;

import com.wcreators.todo_api.configs.security.jwt.JwtProvider;
import com.wcreators.todo_api.constants.Errors;
import com.wcreators.todo_api.constants.Routes;
import com.wcreators.todo_api.exceptions.BadRequestException;
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
import java.util.Optional;

@RestController
@RequestMapping(Routes.Auth.BASE)
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    @PostMapping(Routes.Auth.SIGNUP)
    public String signup(@RequestBody @Valid RegistrationRequestDTO body) {
        if (userService.saveUser(body).isEmpty()) {
            throw new BadRequestException(Errors.AuthError.USERNAME_ALREADY_IN_USE);
        }
        return "OK";
    }

    @PostMapping(Routes.Auth.SIGN_IN)
    public AuthResponseDTO signIn(@RequestBody @Valid AuthRequestDTO body) throws BadRequestException {
        Optional<User> user = userService.findByUsernameAndPassword(body.getUsername(), body.getPassword());
        if (user.isEmpty()) {
            throw new BadRequestException(Errors.AuthError.USERNAME_OR_PASSWORD_INCORRECT);
        }
        String token = jwtProvider.generateToken(user.get().getUsername());
        return AuthResponseDTO.builder().token(token).build();
    }
}
