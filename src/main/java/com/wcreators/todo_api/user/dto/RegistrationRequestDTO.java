package com.wcreators.todo_api.user.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class RegistrationRequestDTO {

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;
}
