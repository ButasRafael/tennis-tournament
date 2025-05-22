// AuthResponseDto.java
package org.example.tennistournament.dto;

import java.util.Map;

public class AuthResponseDto {
    public String accessToken;
    public String refreshToken;
    public String role;
    public String username;
    public String email;
    public Long   id;
    public Long   version;

    public AuthResponseDto(Map<String,String> map) {
        this.accessToken  = map.get("accessToken");
        this.refreshToken = map.get("refreshToken");
        this.role         = map.get("role");
        this.username     = map.get("username");
        this.email        = map.get("email");
        this.id           = Long.valueOf(map.get("id"));
        this.version      = Long.valueOf(map.get("version"));
    }
}
