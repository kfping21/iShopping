package com.example.ishopping.controller;

import com.example.ishopping.entity.Product;
import com.example.ishopping.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        List<Product> products;

        if (category != null && !category.isEmpty()) {
            products = productService.getProductsByCategory(category, page, size);
        } else if (keyword != null && !keyword.isEmpty()) {
            products = productService.searchProducts(keyword, page, size);
        } else {
            products = productService.getAvailableProducts(page, size);
        }

        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product savedProduct = productService.createProduct(product);
        return ResponseEntity.ok(savedProduct);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        Product updatedProduct = productService.updateProduct(id, product);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/seller/my-products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<Product>> getMyProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Product> products = productService.getProductsByCurrentSeller(page, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Product> products = productService.searchProducts(keyword, page, size);
        return ResponseEntity.ok(products);
    }

    /**
     * 获取商品的图片URL
     */
    @GetMapping("/{id}/image")
    public ResponseEntity<String> getProductImage(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product.getImageUrl());
    }

    /**
     * 设置商品主图
     */
    @PutMapping("/{id}/image")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Product> setProductImage(
            @PathVariable Long id,
            @RequestParam String imageUrl) {
        Product product = productService.setProductImage(id, imageUrl);
        return ResponseEntity.ok(product);
    }

    /**
     * 移除商品图片
     */
    @DeleteMapping("/{id}/image")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Product> removeProductImage(@PathVariable Long id) {
        Product product = productService.removeProductImage(id);
        return ResponseEntity.ok(product);
    }
}