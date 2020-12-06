package com.wcreators.todo_api.constants;

import lombok.Getter;

public enum Roles {
    USER("USER"),
    ADMIN("ADMIN");

    @Getter
    private final String name;

    Roles(String name) {
        this.name = name;
    }
}
