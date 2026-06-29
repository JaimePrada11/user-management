package com.jaimeprada.exceptions;

public class AuthenticationException extends UserManagementException {
    public AuthenticationException(String message) {
        super(message);
    }
}