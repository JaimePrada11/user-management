package com.jaimeprada.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordEncoder {

    private PasswordEncoder() {}

    public static String encode(String password) {
        if (password == null || password.isBlank()) {
            return null;
        }

        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public static boolean matches(String password, String encodedPassword) {
        if (password == null || encodedPassword == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(password, encodedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
