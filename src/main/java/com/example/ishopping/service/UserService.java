package com.example.ishopping.service;

import com.example.ishopping.dto.RegisterRequest;
import com.example.ishopping.dto.LoginRequest;
import com.example.ishopping.dto.UserDTO;
import com.example.ishopping.dto.UserUpdateRequest;
import com.example.ishopping.entity.User;
import com.example.ishopping.entity.UserRole;
import com.example.ishopping.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null && !request.getEmail().isEmpty() &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        // 直接存储明文密码
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        // 处理角色设置，提供默认角色
        UserRole role;
        try {
            role = UserRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            role = UserRole.CUSTOMER; // 默认角色
        }
        user.setRole(role);

        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        System.out.println("用户注册成功: " + savedUser.getUsername() + ", 角色: " + savedUser.getRole());
        return savedUser;
    }

    public User authenticate(LoginRequest request) {
        System.out.println("开始用户认证: " + request.getUsername());

        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());
        if (userOptional.isEmpty()) {
            System.out.println("用户不存在: " + request.getUsername());
            throw new RuntimeException("用户不存在或密码错误");
        }

        User user = userOptional.get();
        System.out.println("找到用户: " + user.getUsername());
        System.out.println("数据库密码: " + user.getPassword());
        System.out.println("输入密码: " + request.getPassword());

        // 直接比较明文密码
        boolean passwordMatches = request.getPassword().equals(user.getPassword());
        System.out.println("密码匹配结果: " + passwordMatches);

        if (!passwordMatches) {
            System.out.println("密码错误");
            throw new RuntimeException("用户不存在或密码错误");
        }

        System.out.println("用户认证成功: " + user.getUsername() + ", 角色: " + user.getRole());
        return user;
    }

    // 添加获取用户的方法
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
    }

    /**
     * 获取所有用户列表
     */
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 分页获取用户列表
     */
    public Page<UserDTO> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * 根据ID获取用户
     */
    public UserDTO getUserDTOById(Long userId) {
        User user = getUserById(userId);
        return convertToDTO(user);
    }

    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setRole(userDTO.getRole());
        user.setPassword(userDTO.getPassword()); // 注意：实际项目应加密
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setStatus(userDTO.getStatus() != null ? userDTO.getStatus() : "active");
        User saved = userRepository.save(user);
        return convertToDTO(saved);
    }

    /**
     * 更新用户信息
     */
    public UserDTO updateUser(Long userId, UserUpdateRequest request) {
        User user = getUserById(userId);

        // 更新基本信息
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("用户名已存在");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null) {
            if (!request.getEmail().equals(user.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("邮箱已被注册");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        if (request.getPoints() != null) {
            user.setPoints(request.getPoints());
        }

        if (request.getShippingAddress() != null) {
            user.setShippingAddress(request.getShippingAddress());
        }

        // 更新密码（如果提供了新密码）
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(request.getPassword());
        }

        user.setUpdateTime(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(userId);
    }

    /**
     * 搜索用户
     */
    public List<UserDTO> searchUsers(String keyword) {
        List<User> users = userRepository.findAll().stream()
                .filter(user ->
                        user.getUsername().toLowerCase().contains(keyword.toLowerCase()) ||
                                (user.getEmail() != null && user.getEmail().toLowerCase().contains(keyword.toLowerCase())) ||
                                (user.getPhone() != null && user.getPhone().contains(keyword))
                )
                .collect(Collectors.toList());

        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换User实体为UserDTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setAvatar(user.getAvatar());
        dto.setPoints(user.getPoints());
        dto.setShippingAddress(user.getShippingAddress());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        return dto;
    }


    /**
     * 获取用户统计信息
     */
    public Map<String, Object> getUserStats() {
        List<User> allUsers = userRepository.findAll();

        long totalUsers = allUsers.size();
        long customerCount = allUsers.stream()
                .filter(user -> user.getRole() == UserRole.CUSTOMER)
                .count();
        long sellerCount = allUsers.stream()
                .filter(user -> user.getRole() == UserRole.SELLER)
                .count();
        long adminCount = allUsers.stream()
                .filter(user -> user.getRole() == UserRole.ADMIN)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("customerCount", customerCount);
        stats.put("sellerCount", sellerCount);
        stats.put("adminCount", adminCount);

        return stats;
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        // 明文比较
        if (!oldPassword.equals(user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(newPassword);
        userRepository.save(user);
    }
}