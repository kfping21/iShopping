// UserUpdateRequest.java
package com.example.ishopping.dto;

import com.example.ishopping.entity.UserRole;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private String username;
    private String email;
    private String phone;
    private UserRole role;
    private String avatar;
    private Integer points;
    private String shippingAddress;
    private String password; // 可选，留空则不修改
}