package com.wcreators.todo_api.user.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class RegistrationRequestDTO {

    @NotEmpty
    @Size(min = 2)
    private String username;

    @NotEmpty
    @Size(min = 2)
    private String password;
}
