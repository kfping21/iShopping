package com.example.ishopping.repository;

import com.example.ishopping.entity.Order;
import com.example.ishopping.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 根据用户ID查找订单
    Page<Order> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    // 根据商家ID查找订单
    Page<Order> findBySellerIdOrderByCreateTimeDesc(Long sellerId, Pageable pageable);

    // 查找所有订单（按时间倒序）
    Page<Order> findAllByOrderByCreateTimeDesc(Pageable pageable);

    // 根据订单号搜索
    Page<Order> findByOrderNumberContainingIgnoreCase(String orderNumber, Pageable pageable);

    // 根据商家ID和订单号搜索
    Page<Order> findBySellerIdAndOrderNumberContainingIgnoreCase(Long sellerId, String orderNumber, Pageable pageable);

    // 根据状态查找订单
    Page<Order> findByStatusOrderByCreateTimeDesc(OrderStatus status, Pageable pageable);

    // 根据用户ID和状态查找订单
    Page<Order> findByUserIdAndStatusOrderByCreateTimeDesc(Long userId, OrderStatus status, Pageable pageable);

    // 根据商家ID和状态查找订单
    Page<Order> findBySellerIdAndStatusOrderByCreateTimeDesc(Long sellerId, OrderStatus status, Pageable pageable);

    // 在 OrderRepository 中添加这些统计方法
    @Query("SELECT COUNT(o) FROM Order o WHERE o.sellerId = :sellerId")
    Long countBySellerId(@Param("sellerId") Long sellerId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.sellerId = :sellerId")
    BigDecimal sumTotalAmountBySellerId(@Param("sellerId") Long sellerId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.userId = :userId")
    BigDecimal sumTotalAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    BigDecimal sumTotalAmount();

    Long countByStatus(OrderStatus status);
    Long countBySellerIdAndStatus(Long sellerId, OrderStatus status);
    Long countByUserIdAndStatus(Long userId, OrderStatus status);
}