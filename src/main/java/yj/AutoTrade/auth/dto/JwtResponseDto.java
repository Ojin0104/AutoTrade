package yj.AutoTrade.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JwtResponseDto {
    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private String role;

    public JwtResponseDto(String token, String username, String email, String role) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}