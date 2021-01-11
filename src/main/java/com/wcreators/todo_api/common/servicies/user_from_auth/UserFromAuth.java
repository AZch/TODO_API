package com.wcreators.todo_api.common.servicies.user_from_auth;

import org.springframework.security.core.Authentication;

public interface UserFromAuth {
    Authentication getAuthentication();
}
