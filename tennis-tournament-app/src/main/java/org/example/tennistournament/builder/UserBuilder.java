package org.example.tennistournament.builder;

import org.example.tennistournament.model.Role;
import org.example.tennistournament.model.User;

public class UserBuilder {

    private String username;
    private String email;
    private String password;
    private Role role;

    private UserBuilder() {
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public UserBuilder username(String username) {
        this.username = username;
        return this;
    }

    public UserBuilder email(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder password(String password) {
        this.password = password;
        return this;
    }

    public UserBuilder role(Role role) {
        this.role = role;
        return this;
    }

    public User build() {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }
}
