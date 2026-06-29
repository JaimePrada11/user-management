package com.jaimeprada.security;

import com.jaimeprada.exceptions.AuthenticationException;
import com.jaimeprada.model.User;
import com.jaimeprada.repository.UserRepository;

public class UserAuthenticator extends Authenticator {

    private final UserRepository userRepository;
    private User currentUser;

    public UserAuthenticator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new AuthenticationException(""));

        if(!user.isActive()) {
            throw new AuthenticationException("User is not active");
        }

        if(!verifyPassword(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        this.currentUser = user;
        return user;
    }

    @Override
    public boolean isAuthenticated() {
        return currentUser != null;
    }

    @Override
    public boolean verifyPassword(String password, String hashedPassword) {
        return PasswordEncoder.matches(password, hashedPassword);
    }

    @Override
    public String hashPassword(String password) {
        return PasswordEncoder.encode(password);
    }

    @Override
    public void logout() {
        this.currentUser = null;
    }
}
