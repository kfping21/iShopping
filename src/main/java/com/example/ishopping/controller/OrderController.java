package com.example.ishopping.controller;

import com.example.ishopping.dto.CreateOrderRequest;
import com.example.ishopping.entity.Order;
import com.example.ishopping.entity.OrderStatus;
import com.example.ishopping.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建新订单
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.ok(order);
    }

    /**
     * 获取当前用户的订单列表
     * 顾客：自己的订单 | 商家：自己店铺的订单 | 管理员：所有订单
     */
    @GetMapping
    public ResponseEntity<List<Order>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Order> orders = orderService.getOrdersByCurrentUser(page, size);
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据ID获取订单详情（权限控制）
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * 商家获取自己店铺的订单（专门给商家用的接口）
     */
    @GetMapping("/seller/my-orders")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<Order>> getSellerOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Order> orders = orderService.getOrdersBySeller(page, size);
        return ResponseEntity.ok(orders);
    }

    /**
     * 顾客获取自己的订单（专门给顾客用的接口）
     */
    @GetMapping("/customer/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<Order>> getCustomerOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Order> orders = orderService.getOrdersByCurrentUser(page, size);
        return ResponseEntity.ok(orders);
    }

    /**
     * 管理员获取所有订单
     */
    @GetMapping("/admin/all-orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Order> orders = orderService.getAllOrders(page, size);
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据状态筛选订单
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Order> orders = orderService.getOrdersByStatus(status, page, size);
        return ResponseEntity.ok(orders);
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        Order order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(order);
    }

    /**
     * 取消订单（顾客可以取消自己的待处理订单）
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        Order order = orderService.cancelOrder(id);
        return ResponseEntity.ok(order);
    }

    /**
     * 确认收货（顾客确认收货）
     */
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Order> confirmOrder(@PathVariable Long id) {
        Order order = orderService.confirmOrder(id);
        return ResponseEntity.ok(order);
    }

    /**
     * 搜索订单（管理员和商家可用）
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<List<Order>> searchOrders(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Order> orders = orderService.searchOrders(keyword, page, size);
        return ResponseEntity.ok(orders);
    }

    /**
     * 获取订单统计信息（管理员和商家）
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<?> getOrderStats() {
        Object stats = orderService.getOrderStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 删除订单（管理员和订单所属用户）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 批量删除订单（仅管理员）
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> batchDeleteOrders(@RequestBody List<Long> orderIds) {
        orderService.batchDeleteOrders(orderIds);
        return ResponseEntity.ok().build();
    }
}