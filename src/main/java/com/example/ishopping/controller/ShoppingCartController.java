package com.example.ishopping.controller;

import com.example.ishopping.dto.ShoppingCartRequest;
import com.example.ishopping.entity.ShoppingCart;
import com.example.ishopping.service.ShoppingCartService;
import com.example.ishopping.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping
    public ResponseEntity<?> getCart() {
        try {
            Long userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("用户未登录");
            }

            System.out.println("获取购物车，用户ID: " + userId);
            List<ShoppingCart> cartItems = shoppingCartService.getCartByUserId(userId);

            // 转换为前端需要的格式
            Map<String, Object> response = new HashMap<>();
            response.put("items", cartItems);
            response.put("count", cartItems.stream().mapToInt(ShoppingCart::getQuantity).sum());

            System.out.println("购物车项数量: " + cartItems.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取购物车失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("获取购物车失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody ShoppingCartRequest request) {
        try {
            Long userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("用户未登录");
            }

            System.out.println("添加到购物车，用户ID: " + userId + ", 商品ID: " + request.getProductId() + ", 数量: " + request.getQuantity());
            ShoppingCart cartItem = shoppingCartService.addToCart(request, userId);
            return ResponseEntity.ok(cartItem);
        } catch (Exception e) {
            System.err.println("添加到购物车失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("添加到购物车失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateCartItem(@RequestParam Long productId,
                                            @RequestParam Integer quantity) {
        try {
            Long userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("用户未登录");
            }

            System.out.println("更新购物车，用户ID: " + userId + ", 商品ID: " + productId + ", 新数量: " + quantity);
            ShoppingCart cartItem = shoppingCartService.updateCartItem(productId, quantity, userId);
            if (cartItem == null) {
                return ResponseEntity.ok().body("商品已从购物车移除");
            }
            return ResponseEntity.ok(cartItem);
        } catch (Exception e) {
            System.err.println("更新购物车失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("更新购物车失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestParam Long productId) {
        try {
            Long userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("用户未登录");
            }

            System.out.println("从购物车移除，用户ID: " + userId + ", 商品ID: " + productId);
            shoppingCartService.removeFromCart(productId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("从购物车移除失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("从购物车移除失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart() {
        try {
            Long userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("用户未登录");
            }

            System.out.println("清空购物车，用户ID: " + userId);
            shoppingCartService.clearCart(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("清空购物车失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("清空购物车失败: " + e.getMessage());
        }
    }
}