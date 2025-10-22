package com.example.ishopping.controller;

import com.example.ishopping.dto.LoginRequest;
import com.example.ishopping.dto.RegisterRequest;
import com.example.ishopping.entity.User;
import com.example.ishopping.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            System.out.println("开始用户注册: " + request.getUsername());
            User user = userService.register(request);

            // 创建用户信息响应（不包含密码）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole());

            System.out.println("用户注册成功: " + user.getUsername());
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            System.err.println("用户注册失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("注册失败: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        try {
            System.out.println("开始用户登录: " + request.getUsername());
            User user = userService.authenticate(request);

            // 将用户信息存入session
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());

            // 创建用户信息响应（不包含密码）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole());

            System.out.println("用户登录成功: " + user.getUsername());
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            System.err.println("用户登录失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("退出成功");
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAuth(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body("用户未登录");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userId);
        userInfo.put("username", session.getAttribute("username"));
        userInfo.put("role", session.getAttribute("role"));

        return ResponseEntity.ok(userInfo);
    }
}