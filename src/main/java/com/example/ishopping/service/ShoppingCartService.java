package com.example.ishopping.service;

import com.example.ishopping.dto.ShoppingCartRequest;
import com.example.ishopping.entity.ShoppingCart;
import com.example.ishopping.entity.Product;
import com.example.ishopping.entity.User;
import com.example.ishopping.repository.ShoppingCartRepository;
import com.example.ishopping.repository.ProductRepository;
import com.example.ishopping.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ShoppingCartService {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ShoppingCart> getCartByUserId(Long userId) {
        return shoppingCartRepository.findByUserId(userId);
    }

    @Transactional
    public ShoppingCart addToCart(ShoppingCartRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("商品库存不足");
        }

        Optional<ShoppingCart> existingCart = shoppingCartRepository.findByUserIdAndProductId(userId, request.getProductId());

        if (existingCart.isPresent()) {
            ShoppingCart cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + request.getQuantity());
            return shoppingCartRepository.save(cart);
        } else {
            ShoppingCart cart = new ShoppingCart();
            cart.setUser(user);
            cart.setProduct(product);
            cart.setQuantity(request.getQuantity());
            return shoppingCartRepository.save(cart);
        }
    }

    @Transactional
    public void removeFromCart(Long productId, Long userId) {
        shoppingCartRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public void clearCart(Long userId) {
        shoppingCartRepository.deleteByUserId(userId);
    }

    @Transactional
    public ShoppingCart updateCartItem(Long productId, Integer quantity, Long userId) {
        ShoppingCart cart = shoppingCartRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));

        if (quantity <= 0) {
            shoppingCartRepository.deleteByUserIdAndProductId(userId, productId);
            return null;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (product.getStock() < quantity) {
            throw new RuntimeException("商品库存不足");
        }

        cart.setQuantity(quantity);
        return shoppingCartRepository.save(cart);
    }
}