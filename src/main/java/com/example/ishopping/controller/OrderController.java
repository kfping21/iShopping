package com.example.ishopping.controller;

import com.example.ishopping.dto.UserDTO;
import com.example.ishopping.dto.OrderItemResponse;
import com.example.ishopping.dto.OrderResponse;
import com.example.ishopping.dto.CreateOrderRequest;
import com.example.ishopping.entity.Order;
import com.example.ishopping.entity.OrderItem;
import com.example.ishopping.entity.OrderStatus;
import com.example.ishopping.entity.User;
import com.example.ishopping.entity.UserRole;
import com.example.ishopping.repository.OrderRepository;
import com.example.ishopping.repository.UserRepository;
import com.example.ishopping.service.OrderService;
import com.example.ishopping.service.UserService;
import com.example.ishopping.util.UserContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 创建新订单 -> 返回 DTO
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.ok(toDto(order));
    }

    /**
     * 获取当前用户的订单列表（返回 DTO 列表）
     * 顾客：自己的订单 | 商家：自己店铺的订单 | 管理员：所有订单
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Order> orders = orderService.getOrdersByCurrentUser(page, size);
        List<OrderResponse> resp = orders.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    /**
     * 根据ID获取订单详情（返回 DTO，包含 items）
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(toDto(order));
    }

    /**
     * 根据订单号获取支付订单信息（用于支付页）
     */
    @GetMapping("/payment/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderForPayment(@PathVariable String orderNumber) {
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (!orderOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Order order = orderOpt.get();

        // 权限检查：只能查看自己的订单
        User currentUser = getCurrentUser();
        if (!order.getUserId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        // 只能支付待支付的订单
        if (order.getStatus() != OrderStatus.PENDING) {
            return ResponseEntity.status(400).build();
        }

        return ResponseEntity.ok(toDto(order));
    }

    /**
     * 商家获取自己店铺的订单（专门给商家用的接口） -> DTO 列表
     */
    @GetMapping("/seller/my-orders")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<OrderResponse>> getSellerOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Order> orders = orderService.getOrdersBySeller(page, size);
        return ResponseEntity.ok(orders.stream().map(this::toDto).collect(Collectors.toList()));
    }

    /**
     * 顾客获取自己的订单（专门给顾客用的接口）
     */
    @GetMapping("/customer/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Order> orders = orderService.getOrdersByCurrentUser(page, size);
        return ResponseEntity.ok(orders.stream().map(this::toDto).collect(Collectors.toList()));
    }

    /**
     * 管理员获取所有订单
     */
    @GetMapping("/admin/all-orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Order> orders = orderService.getAllOrders(page, size);
        return ResponseEntity.ok(orders.stream().map(this::toDto).collect(Collectors.toList()));
    }

    /**
     * 根据状态筛选订单
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Order> orders = orderService.getOrdersByStatus(status, page, size);
        return ResponseEntity.ok(orders.stream().map(this::toDto).collect(Collectors.toList()));
    }

    /**
     * 更新订单状态 -> 返回 DTO
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        Order order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(toDto(order));
    }

    /**
     * 取消订单（顾客可以取消自己的待处理订单）
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        Order order = orderService.cancelOrder(id);
        return ResponseEntity.ok(toDto(order));
    }

    /**
     * 确认收货（顾客确认收货）
     */
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable Long id) {
        Order order = orderService.confirmOrder(id);
        return ResponseEntity.ok(toDto(order));
    }

    /**
     * 搜索订单（管理员和商家可用）
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<List<OrderResponse>> searchOrders(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Order> orders = orderService.searchOrders(keyword, page, size);
        return ResponseEntity.ok(orders.stream().map(this::toDto).collect(Collectors.toList()));
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

    // ---------- 辅助方法 ----------

    private OrderResponse toDto(Order o) {
        if (o == null) return null;
        OrderResponse r = new OrderResponse();
        r.setId(o.getId());
        r.setOrderNumber(o.getOrderNumber());
        r.setUserId(o.getUserId());
        r.setSellerId(o.getSellerId());
        r.setTotalAmount(o.getTotalAmount());
        r.setDiscountAmount(o.getDiscountAmount());
        r.setShippingFee(o.getShippingFee());
        r.setActualAmount(o.getActualAmount());
        r.setStatus(o.getStatus() != null ? o.getStatus().name() : null);
        r.setShippingAddress(o.getShippingAddress());
        r.setReceiverName(o.getReceiverName());
        r.setReceiverPhone(o.getReceiverPhone());
        r.setPaymentMethod(o.getPaymentMethod());
        r.setCreateTime(o.getCreateTime());
        r.setUpdateTime(o.getUpdateTime());

        List<OrderItemResponse> items = (o.getOrderItems() == null) ? List.of() :
                o.getOrderItems().stream().map(this::toItemDto).collect(Collectors.toList());
        r.setItems(items);

        // 正确查用户并赋值
        UserDTO userDto = null;
        if (o.getUserId() != null) {
            User userEntity = userRepository.findById(o.getUserId()).orElse(null);
            if (userEntity != null) {
                userDto = new UserDTO();
                userDto.setId(userEntity.getId());
                userDto.setUsername(userEntity.getUsername());
                userDto.setEmail(userEntity.getEmail());
                userDto.setPhone(userEntity.getPhone());
                // 可加其它字段
            }
        }
        r.setUser(userDto);

        return r;
    }

    private OrderItemResponse toItemDto(OrderItem it) {
        if (it == null) return null;
        String productName = null;
        String productImageUrl = null;
        try {
            if (it.getProduct() != null) {
                productName = it.getProduct().getName();
                productImageUrl = it.getProduct().getImageUrl(); // 假设有这个字段
            }
        } catch (Exception ignore) { productName = null; }

        OrderItemResponse ir = new OrderItemResponse(
                it.getId(),
                it.getProductId(),
                productName,
                it.getQuantity(),
                it.getPrice()
        );
        ir.setProductImageUrl(productImageUrl);
        return ir;
    }

    /**
     * 获取当前登录用户（供Controller使用）
     */
    private User getCurrentUser() {
        Long userId = UserContext.getCurrentUserId();
        UserRole role = UserContext.getCurrentUserRole();
        String username = UserContext.getCurrentUsername();

        if (userId == null || role == null || username == null) {
            throw new RuntimeException("用户未登录");
        }

        User user = new User();
        user.setId(userId);
        user.setRole(role);
        user.setUsername(username);

        return user;
    }
}