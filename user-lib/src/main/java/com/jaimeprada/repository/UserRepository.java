package com.jaimeprada.repository;

import com.jaimeprada.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    User update(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsById(Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findAll();

    List<User> findAllByActive();

    List<User> findAllDeleted();

    boolean delete(Long id);

    boolean softDelete(Long id);


}
