package com.example.ishopping.util;

import com.example.ishopping.entity.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserContext {

    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<UserRole> currentUserRole = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUsername = new ThreadLocal<>();

    public static void setCurrentUser(Long userId, UserRole role, String username) {
        currentUserId.set(userId);
        currentUserRole.set(role);
        currentUsername.set(username);
    }

    public static Long getCurrentUserId() {
        return currentUserId.get();
    }

    public static UserRole getCurrentUserRole() {
        return currentUserRole.get();
    }

    public static String getCurrentUsername() {
        return currentUsername.get();
    }

    public static void clear() {
        currentUserId.remove();
        currentUserRole.remove();
        currentUsername.remove();
    }
}