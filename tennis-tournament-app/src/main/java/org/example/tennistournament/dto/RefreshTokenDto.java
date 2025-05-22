// RefreshTokenDto.java
package org.example.tennistournament.dto;

import java.util.Map;

public class RefreshTokenDto {
    public String accessToken;
    public String refreshToken;

    public RefreshTokenDto(Map<String,String> map) {
        this.accessToken  = map.get("accessToken");
        this.refreshToken = map.get("refreshToken");
    }
}
