package org.example.tennistournament.dto;

import org.example.tennistournament.model.User;
import org.example.tennistournament.model.Role;

public class UserDto {
    public Long   id;
    public String username;
    public String email;
    public Role   role;

    public UserDto(User u) {
        this.id       = u.getId();
        this.username = u.getUsername();
        this.email    = u.getEmail();
        this.role     = u.getRole();
    }
}
