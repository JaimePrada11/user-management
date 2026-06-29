package com.jaimeprada.exceptions;

public class UserNotFoundException extends UserManagementException {

    public UserNotFoundException(String identifier) {
        super("User not found: " + identifier);
    }
}
