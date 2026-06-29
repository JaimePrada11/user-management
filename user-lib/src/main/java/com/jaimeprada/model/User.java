package com.jaimeprada.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {

    private Long id;
    private String username;
    private String email;
    private String password;
    private String fullName;
    private boolean active;
    private Role role;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;

    public User() {}

    public User(Long id, String username, String email, String password, String fullName, Role role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.active = true;
        this.role = role;

    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime dateCreated) { this.dateCreated = dateCreated; }

    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime dateUpdated) { this.dateUpdated = dateUpdated; }

    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", email=" + email + ", password=" + password + ", fullName="
                + fullName + ", active=" + active + ", dateCreated=" + dateCreated + ", dateUpdated=" + dateUpdated + "]";
    }
}
