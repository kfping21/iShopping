package com.example.ishopping.dto;

import com.example.ishopping.entity.User;
import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private User user;

    public AuthResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }
}