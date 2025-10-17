package com.example.ishopping.repository;

import com.example.ishopping.entity.Product;
import com.example.ishopping.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 根据状态查询商品（带分页）
    Page<Product> findByStatusOrderByCreateTimeDesc(ProductStatus status, Pageable pageable);

    // 根据商家ID查询商品
    List<Product> findBySellerId(Long sellerId);

    // 根据商家ID查询商品（带分页）
    Page<Product> findBySellerIdOrderByCreateTimeDesc(Long sellerId, Pageable pageable);

    // 根据分类和状态查询商品（带分页）
    Page<Product> findByCategoryAndStatusOrderByCreateTimeDesc(String category, ProductStatus status, Pageable pageable);

    // 搜索商品（根据名称或描述）
    @Query("SELECT p FROM Product p WHERE (p.name LIKE %:keyword% OR p.description LIKE %:keyword%) AND p.status = :status ORDER BY p.createTime DESC")
    Page<Product> findByNameContainingOrDescriptionContainingAndStatus(
            @Param("keyword") String keyword,
            @Param("status") ProductStatus status,
            Pageable pageable);

    // 根据分类查询商品
    List<Product> findByCategory(String category);

    // 根据商家ID和状态查询商品
    Page<Product> findBySellerIdAndStatusOrderByCreateTimeDesc(Long sellerId, ProductStatus status, Pageable pageable);

    // 根据多个状态查询商品
    Page<Product> findByStatusInOrderByCreateTimeDesc(List<ProductStatus> statuses, Pageable pageable);
}