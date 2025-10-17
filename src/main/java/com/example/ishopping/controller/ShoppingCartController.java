package com.example.ishopping.controller;

import com.example.ishopping.dto.ShoppingCartRequest;
import com.example.ishopping.entity.ShoppingCart;
import com.example.ishopping.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping
    public ResponseEntity<List<ShoppingCart>> getCart(@RequestParam Long userId) {
        List<ShoppingCart> cartItems = shoppingCartService.getCartByUserId(userId);
        return ResponseEntity.ok(cartItems);
    }

    @PostMapping("/add")
    public ResponseEntity<ShoppingCart> addToCart(@RequestBody ShoppingCartRequest request,
                                                  @RequestParam Long userId) {
        ShoppingCart cartItem = shoppingCartService.addToCart(request, userId);
        return ResponseEntity.ok(cartItem);
    }

    @PutMapping("/update")
    public ResponseEntity<ShoppingCart> updateCartItem(@RequestParam Long productId,
                                                       @RequestParam Integer quantity,
                                                       @RequestParam Long userId) {
        ShoppingCart cartItem = shoppingCartService.updateCartItem(productId, quantity, userId);
        return ResponseEntity.ok(cartItem);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestParam Long productId,
                                            @RequestParam Long userId) {
        shoppingCartService.removeFromCart(productId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@RequestParam Long userId) {
        shoppingCartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }
}