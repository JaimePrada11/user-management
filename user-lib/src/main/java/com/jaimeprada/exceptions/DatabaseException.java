package com.jaimeprada.exceptions;

public class DatabaseException extends UserManagementException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

}