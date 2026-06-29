package com.jaimeprada.security;

import com.jaimeprada.model.User;

public abstract class Authenticator {

    public abstract User authenticate(String username, String password) ;

    public abstract boolean isAuthenticated();

    public abstract boolean verifyPassword(String password, String hashedPassword);

    public abstract String hashPassword(String password);

    public abstract void logout();

}
