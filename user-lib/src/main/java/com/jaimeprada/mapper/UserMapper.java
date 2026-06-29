package com.jaimeprada.mapper;

import com.jaimeprada.model.Role;
import com.jaimeprada.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public record UserMapper(
  Long id,
  String username,
  String email,
  String password,
  String fullName,
  boolean active,
  String role,
  LocalDateTime dateCreated,
  LocalDateTime dateUpdated
){

    public static UserMapper from(ResultSet rs) throws SQLException {
        return new UserMapper(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("full_name"),
                rs.getBoolean("active"),
                rs.getString("role"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("updated_at", LocalDateTime.class)
        );
    }

    public User toUser() {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setActive(active);
        user.setRole(Role.valueOf(role));
        user.setDateCreated(dateCreated);
        user.setDateUpdated(dateUpdated);

        return user;
    }
}
