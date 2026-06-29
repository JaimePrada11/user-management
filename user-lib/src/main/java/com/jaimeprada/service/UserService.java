package com.jaimeprada.service;

import com.jaimeprada.model.User;

import java.util.List;

public interface UserService {

    User create(User user);

    User updateUser(User user, String newPassword);

    List<User> findAll();

    User getUserById(Long id);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    void deleteUser(Long id);
}
