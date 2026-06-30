package com.jaimeprada.service;

import com.jaimeprada.exceptions.DuplicateUserException;
import com.jaimeprada.exceptions.UserNotFoundException;
import com.jaimeprada.model.User;
import com.jaimeprada.repository.UserRepository;
import com.jaimeprada.security.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(User user) {

        validateUser(user);

        if(userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateUserException("username", user.getUsername());
        }
        if(userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateUserException("email", user.getEmail());
        }

        user.setPassword(PasswordEncoder.encode(user.getPassword()));

        LocalDateTime now = LocalDateTime.now();
        user.setDateCreated(now);
        user.setDateUpdated(now);
        user.setActive(true);

        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user, String newPassword) {
        if(user.getId() == null)  {
            throw new RuntimeException("ID is required to update a user");
        }

        User existing = userRepository.findById(user.getId())
                        .orElseThrow(()-> new UserNotFoundException("ID: " + user.getId()));

        userRepository.findByUsername(user.getUsername())
                .ifPresent(u -> {
                    if(!u.getId().equals(user.getId())) {
                        throw new DuplicateUserException("username", user.getUsername());
                    }
                });

        userRepository.findByEmail(user.getEmail())
                .ifPresent(u -> {
                    if(!u.getId().equals(user.getId())) {
                        throw new DuplicateUserException("email", user.getEmail());
                    }
                });

        if(newPassword != null && !newPassword.isBlank()) {
            user.setPassword(PasswordEncoder.encode(newPassword));
        } else {
            user.setPassword(existing.getPassword());
        }

        user.setDateUpdated(LocalDateTime.now());
        return userRepository.update(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("ID: " + id));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Username: " + username));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Email: " + email));
    }


    @Override
    public void softDeleteUser(Long id) {
        boolean deleted = userRepository.softDelete(id);

        if(!deleted) {
            throw new UserNotFoundException("ID: " + id);
        }
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.delete(id);
    }

    private void validateUser(User user) {
        if (user == null) throw new IllegalArgumentException("User is required");

        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (user.getFullName() == null || user.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }

        if (user.getRole() == null) {
            throw new IllegalArgumentException("Role is required");
        }
    }
}

