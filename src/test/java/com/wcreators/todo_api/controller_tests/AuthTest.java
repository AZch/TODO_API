package com.wcreators.todo_api.controller_tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcreators.todo_api.configs.security.details.CustomUserDetailsService;
import com.wcreators.todo_api.configs.security.jwt.JwtFilter;
import com.wcreators.todo_api.configs.security.jwt.JwtProvider;
import com.wcreators.todo_api.constants.Roles;
import com.wcreators.todo_api.constants.Routes;
import com.wcreators.todo_api.user.controllers.auth.AuthController;
import com.wcreators.todo_api.user.dto.AuthRequestDTO;
import com.wcreators.todo_api.user.dto.RegistrationRequestDTO;
import com.wcreators.todo_api.user.entities.Role;
import com.wcreators.todo_api.user.entities.User;
import com.wcreators.todo_api.user.repositories.RoleRepository;
import com.wcreators.todo_api.user.repositories.UserRepository;
import com.wcreators.todo_api.user.services.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({
        JwtFilter.class,
        JwtProvider.class,
        UserService.class,
        CustomUserDetailsService.class
})
public class AuthTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Nested
    class SignIn {
        @Test
        public void shouldFailureSignInUserNotExist() throws Exception {
            String username = "username";

            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

            AuthRequestDTO authRequestDTO = AuthRequestDTO.builder()
                    .username(username)
                    .password("password")
                    .build();
            mockMvc
                    .perform(
                            post(String.format("%s%s", Routes.Auth.BASE, Routes.Auth.SIGN_IN))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(authRequestDTO))
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        public void shouldFailureSignInUsernameIncorrect() throws Exception {
            String actualUsername = "actual_username";
            String expectedUsername = "expected_username";
            String password = "password";

            when(passwordEncoder.matches(password, password)).thenReturn(true);

            when(userRepository.findByUsername(expectedUsername)).thenReturn(
                    Optional.of(
                            User.builder()
                                    .username(expectedUsername)
                                    .password(password)
                                    .build()
                    )
            );

            AuthRequestDTO authRequestDTO = AuthRequestDTO.builder()
                    .username(actualUsername)
                    .password(password)
                    .build();
            mockMvc
                    .perform(
                            post(String.format("%s%s", Routes.Auth.BASE, Routes.Auth.SIGN_IN))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(authRequestDTO))
                    )
                    .andExpect(status().isBadRequest());

        }

        @Test
        public void shouldFailureSignInPasswordIncorrect() throws Exception {
            String username = "username";
            String actualPassword = "actual_password";
            String expectedPassword = "expected_password";

            when(passwordEncoder.matches(actualPassword, expectedPassword)).thenReturn(false);

            when(userRepository.findByUsername(username)).thenReturn(
                    Optional.of(
                            User.builder()
                                    .username(username)
                                    .password(expectedPassword)
                                    .build()
                    )
            );

            AuthRequestDTO authRequestDTO = AuthRequestDTO.builder()
                    .username(username)
                    .password(actualPassword)
                    .build();
            mockMvc
                    .perform(
                            post(String.format("%s%s", Routes.Auth.BASE, Routes.Auth.SIGN_IN))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(authRequestDTO))
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        public void shouldFailureSignInValidation() throws Exception {
            AuthRequestDTO authRequestDTO = AuthRequestDTO.builder()
                    .username("")
                    .password("")
                    .build();
            mockMvc
                    .perform(
                            post(String.format("%s%s", Routes.Auth.BASE, Routes.Auth.SIGN_IN))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(authRequestDTO))
                    )
                    .andExpect(status().isBadRequest());

        }

        @Test
        public void shouldSuccessfullySignIn() throws Exception {
            String username = "username";
            String password = "password";

            when(passwordEncoder.matches(password, password)).thenReturn(true);

            when(userRepository.findByUsername(username)).thenReturn(
                    Optional.of(
                            User.builder()
                                    .username(username)
                                    .password(password)
                                    .build()
                    )
            );

            AuthRequestDTO authRequestDTO = AuthRequestDTO.builder()
                    .username(username)
                    .password(password)
                    .build();
            mockMvc
                    .perform(
                            post(String.format("%s%s", Routes.Auth.BASE, Routes.Auth.SIGN_IN))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(authRequestDTO))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value(jwtProvider.generateToken(username)));
        }
    }

    @Nested
    class SignUp {
        @Test
        public void shouldFailureSignUpUserAlreadyExist() throws Exception {
            String username = "username";
            String password = "password";

            when(roleRepository.findByName(Roles.USER.getName())).thenReturn(
                    Optional.of(
                        Role.builder()
                                .name(Roles.USER.getName())
                                .build()
                    )
            );

            when(userRepository.findByUsername(username)).thenReturn(
                    Optional.of(
                            User.builder()
                                    .username(username)
                                    .password(password)
                                    .role(
                                            Role.builder()
                                                    .name(Roles.USER.getName())
                                                    .build()
                                    )
                                    .build()
                    )
            );

            RegistrationRequestDTO registrationRequestDTO = RegistrationRequestDTO.builder()
                    .username(username)
                    .password(password)
                    .build();
            mockMvc
                    .perform(
                            post(String.format("%s%s", Routes.Auth.BASE, Routes.Auth.SIGNUP))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(registrationRequestDTO))
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        public void shouldFailureSignUpValidation() throws Exception {
            RegistrationRequestDTO registrationRequestDTO = RegistrationRequestDTO.builder()
                    .username("")
                    .password("")
                    .build();
            mockMvc
                    .perform(
                            post(String.format("%s%s", Routes.Auth.BASE, Routes.Auth.SIGNUP))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(registrationRequestDTO))
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        public void shouldSuccessfullySignUp() throws Exception {
            String username = "username";
            String password = "password";
            Role role = Role.builder()
                    .name(Roles.USER.getName())
                    .build();
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .build();

            when(passwordEncoder.encode(password)).thenReturn(password);

            when(roleRepository.findByName(Roles.USER.getName())).thenReturn(Optional.of(role));

            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

            when(userRepository.save(
                    User.builder()
                            .username(username)
                            .password(passwordEncoder.encode(password))
                            .role(role)
                            .build()
            )).thenReturn(user);

            RegistrationRequestDTO registrationRequestDTO = RegistrationRequestDTO.builder()
                    .username(username)
                    .password(password)
                    .build();
            mockMvc
                    .perform(
                            post(String.format("%s%s", Routes.Auth.BASE, Routes.Auth.SIGNUP))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(registrationRequestDTO))
                    )
                    .andExpect(status().isOk());
        }
    }
}
