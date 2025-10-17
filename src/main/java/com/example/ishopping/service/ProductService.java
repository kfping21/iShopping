package com.example.ishopping.service;

import com.example.ishopping.entity.Product;
import com.example.ishopping.entity.ProductStatus;
import com.example.ishopping.entity.User;
import com.example.ishopping.repository.ProductRepository;
import com.example.ishopping.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取在售商品列表（带分页）
     */
    public List<Product> getAvailableProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByStatusOrderByCreateTimeDesc(ProductStatus.ON_SALE, pageable);
        return productPage.getContent();
    }

    /**
     * 根据ID获取商品
     */
    public Product getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.orElseThrow(() -> new RuntimeException("商品不存在"));
    }

    /**
     * 创建商品（自动设置当前商家）
     */
    public Product createProduct(Product product) {
        // 获取当前登录的用户
        User currentUser = getCurrentUser();

        // 验证用户角色是否为商家
        if (!currentUser.getRole().name().equals("SELLER")) {
            throw new RuntimeException("只有商家可以创建商品");
        }

        // 设置商品基本信息
        product.setSellerId(currentUser.getId());
        product.setStatus(ProductStatus.ON_SALE);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());

        // 验证必要字段
        validateProduct(product);

        return productRepository.save(product);
    }

    /**
     * 更新商品（只能更新自己的商品）
     */
    public Product updateProduct(Long id, Product productDetails) {
        Product existingProduct = getProductById(id);
        User currentUser = getCurrentUser();

        // 验证商品所有权
        if (!existingProduct.getSellerId().equals(currentUser.getId())) {
            throw new RuntimeException("只能修改自己的商品");
        }

        // 更新允许修改的字段
        if (productDetails.getName() != null) {
            existingProduct.setName(productDetails.getName());
        }
        if (productDetails.getDescription() != null) {
            existingProduct.setDescription(productDetails.getDescription());
        }
        if (productDetails.getPrice() != null) {
            existingProduct.setPrice(productDetails.getPrice());
        }
        if (productDetails.getStock() != null) {
            existingProduct.setStock(productDetails.getStock());
        }
        if (productDetails.getCategory() != null) {
            existingProduct.setCategory(productDetails.getCategory());
        }
        if (productDetails.getImageUrl() != null) {
            existingProduct.setImageUrl(productDetails.getImageUrl());
        }

        existingProduct.setUpdateTime(LocalDateTime.now());

        return productRepository.save(existingProduct);
    }

    /**
     * 删除商品（软删除）
     */
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        User currentUser = getCurrentUser();

        // 验证商品所有权
        if (!product.getSellerId().equals(currentUser.getId())) {
            throw new RuntimeException("只能删除自己的商品");
        }

        // 软删除：修改状态为下架
        product.setStatus(ProductStatus.OFF_SALE);
        product.setUpdateTime(LocalDateTime.now());
        productRepository.save(product);
    }

    /**
     * 获取当前商家的商品列表（带分页）
     */
    public List<Product> getProductsByCurrentSeller(int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findBySellerIdOrderByCreateTimeDesc(currentUser.getId(), pageable);
        return productPage.getContent();
    }

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未认证");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 验证商品信息
     */
    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new RuntimeException("商品名称不能为空");
        }
        if (product.getPrice() == null || product.getPrice().doubleValue() <= 0) {
            throw new RuntimeException("商品价格必须大于0");
        }
        if (product.getStock() == null || product.getStock() < 0) {
            throw new RuntimeException("商品库存不能为负数");
        }
        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            throw new RuntimeException("商品分类不能为空");
        }
    }
}