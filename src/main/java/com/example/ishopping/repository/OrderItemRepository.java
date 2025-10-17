package com.example.ishopping.repository;

import com.example.ishopping.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 根据订单ID查找订单项
    List<OrderItem> findByOrderId(Long orderId);

    // 根据订单ID删除订单项
    void deleteByOrderId(Long orderId);
}