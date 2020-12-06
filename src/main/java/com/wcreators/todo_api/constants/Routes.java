package com.wcreators.todo_api.constants;

public class Routes {
    public static class Auth {
        public static final String BASE = "/auth";

        public static final String SIGN_IN = "/signin";
        public static final String SIGNUP = "/signup";
    }

    public static class Notes {
        public static final String BASE = "/notes";

        public static final String GET_ONE = "/{id}";
        public static final String EDIT = "/{id}";
        public static final String DELETE = "/{id}";
    }
}
