package com.bnm.recouvrement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private boolean firstLogin;
    
    public AuthResponse(String token) {
        this.token = token;
    }
}
