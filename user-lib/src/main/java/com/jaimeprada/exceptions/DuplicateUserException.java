package com.jaimeprada.exceptions;

public class DuplicateUserException extends UserManagementException{

    public DuplicateUserException(String field, String value) {
        super("Duplicate " + field + ": " + value + " already exists");
    }
}
