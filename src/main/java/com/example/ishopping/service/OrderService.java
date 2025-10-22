package com.example.ishopping.service;

import com.example.ishopping.dto.CreateOrderRequest;
import com.example.ishopping.entity.*;
import com.example.ishopping.repository.OrderRepository;
import com.example.ishopping.repository.UserRepository;
import com.example.ishopping.repository.ProductRepository;
import com.example.ishopping.repository.OrderItemRepository;
import com.example.ishopping.util.UserContext;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    /**
     * 创建订单
     */
    public Order createOrder(CreateOrderRequest request) {
        User currentUser = getCurrentUser();

        // 验证用户角色
        if (currentUser.getRole() != UserRole.CUSTOMER) {
            throw new RuntimeException("只有顾客可以创建订单");
        }

        // 创建订单对象
        Order order = new Order();
        String orderNumber = generateOrderNumber();
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(currentUser.getId());
        order.setShippingAddress(request.getShippingAddress());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        order.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
        order.setStatus(OrderStatus.PENDING);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        System.out.println("生成的订单号: " + orderNumber);
        System.out.println("收货地址: " + request.getShippingAddress());
        System.out.println("订单项数量: " + request.getOrderItems().size());

        // 处理订单项
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        Long sellerId = null;

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在: " + itemRequest.getProductId()));

            // 检查库存
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new RuntimeException("商品库存不足: " + product.getName() + "，库存: " + product.getStock() + "，需求: " + itemRequest.getQuantity());
            }

            // 检查商品状态
            if (product.getStatus() != ProductStatus.ON_SALE) {
                throw new RuntimeException("商品已下架: " + product.getName());
            }

            // 设置商家ID（取第一个商品的商家）
            if (sellerId == null) {
                sellerId = product.getSellerId();
            }

            // 验证所有商品是否属于同一个商家
            if (!product.getSellerId().equals(sellerId)) {
                throw new RuntimeException("订单中的所有商品必须属于同一个商家");
            }

            // 创建订单项
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setCreateTime(LocalDateTime.now());
            orderItems.add(orderItem);

            // 计算金额
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        if (sellerId == null) {
            throw new RuntimeException("订单必须包含至少一个商品");
        }

        order.setSellerId(sellerId);
        order.setTotalAmount(totalAmount);
        order.setActualAmount(totalAmount
                .subtract(order.getDiscountAmount())
                .add(order.getShippingFee()));

        // 保存订单
        Order savedOrder = orderRepository.save(order);
        System.out.println("订单创建成功，ID: " + savedOrder.getId());

        // 保存订单项并更新库存
        for (OrderItem item : orderItems) {
            item.setOrderId(savedOrder.getId());
            orderItemRepository.save(item);

            // 减少库存
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在: " + item.getProductId()));
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        // 设置订单项关联（已在事务内）
        savedOrder.setOrderItems(orderItems);
        return savedOrder;
    }

    /**
     * 根据ID获取订单（权限控制）
     */
    public Order getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        // 权限检查
        checkOrderPermission(order);

        // 初始化关联，确保返回时可序列化
        initializeOrders(Collections.singletonList(order));

        return order;
    }

    /**
     * 获取当前用户的订单列表
     */
    public List<Order> getOrdersByCurrentUser(int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orderPage;
        if (currentUser.getRole() == UserRole.ADMIN) {
            // 管理员：查看所有订单
            orderPage = orderRepository.findAllByOrderByCreateTimeDesc(pageable);
        } else if (currentUser.getRole() == UserRole.SELLER) {
            // 商家：查看自己店铺的订单
            orderPage = orderRepository.findBySellerIdOrderByCreateTimeDesc(currentUser.getId(), pageable);
        } else {
            // 顾客：查看自己的订单
            orderPage = orderRepository.findByUserIdOrderByCreateTimeDesc(currentUser.getId(), pageable);
        }

        List<Order> orders = orderPage.getContent();
        initializeOrders(orders);
        return orders;
    }

    /**
     * 获取商家的订单列表
     */
    public List<Order> getOrdersBySeller(int page, int size) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != UserRole.SELLER) {
            throw new RuntimeException("只有商家可以查看店铺订单");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findBySellerIdOrderByCreateTimeDesc(currentUser.getId(), pageable);
        List<Order> orders = orderPage.getContent();
        initializeOrders(orders);
        return orders;
    }

    /**
     * 获取所有订单（管理员）
     */
    public List<Order> getAllOrders(int page, int size) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("只有管理员可以查看所有订单");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findAllByOrderByCreateTimeDesc(pageable);
        List<Order> orders = orderPage.getContent();
        initializeOrders(orders);
        return orders;
    }

    /**
     * 根据状态筛选订单
     */
    public List<Order> getOrdersByStatus(OrderStatus status, int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orderPage;
        if (currentUser.getRole() == UserRole.ADMIN) {
            orderPage = orderRepository.findByStatusOrderByCreateTimeDesc(status, pageable);
        } else if (currentUser.getRole() == UserRole.SELLER) {
            orderPage = orderRepository.findBySellerIdAndStatusOrderByCreateTimeDesc(
                    currentUser.getId(), status, pageable);
        } else {
            orderPage = orderRepository.findByUserIdAndStatusOrderByCreateTimeDesc(
                    currentUser.getId(), status, pageable);
        }

        List<Order> orders = orderPage.getContent();
        initializeOrders(orders);
        return orders;
    }

    /**
     * 更新订单状态
     */
    public Order updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = getOrderById(id);
        User currentUser = getCurrentUser();

        // 状态流转验证
        validateStatusTransition(order.getStatus(), newStatus, currentUser.getRole());

        // 状态更新权限检查
        if (currentUser.getRole() == UserRole.CUSTOMER) {
            // 顾客只能取消订单或确认收货
            if (newStatus != OrderStatus.CANCELLED && newStatus != OrderStatus.COMPLETED) {
                throw new RuntimeException("顾客只能取消订单或确认收货");
            }
            if (!order.getUserId().equals(currentUser.getId())) {
                throw new RuntimeException("只能更新自己的订单状态");
            }
        }

        if (currentUser.getRole() == UserRole.SELLER) {
            // 商家只能更新自己店铺的订单状态（除了完成状态）
            if (!order.getSellerId().equals(currentUser.getId())) {
                throw new RuntimeException("只能更新自己店铺的订单状态");
            }
            if (newStatus == OrderStatus.COMPLETED) {
                throw new RuntimeException("商家不能确认订单完成，只能由顾客确认收货");
            }
        }

        order.setStatus(newStatus);
        order.setUpdateTime(LocalDateTime.now());

        return orderRepository.save(order);
    }

    /**
     * 取消订单
     */
    public Order cancelOrder(Long id) {
        Order order = getOrderById(id);
        User currentUser = getCurrentUser();

        // 权限检查：顾客可以取消自己的订单，商家可以取消自己店铺的订单
        if (currentUser.getRole() == UserRole.CUSTOMER && !order.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("只能取消自己的订单");
        }
        if (currentUser.getRole() == UserRole.SELLER && !order.getSellerId().equals(currentUser.getId())) {
            throw new RuntimeException("只能取消自己店铺的订单");
        }

        // 只能取消待处理或已支付的订单
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("当前状态的订单不能取消");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdateTime(LocalDateTime.now());

        // 恢复库存
        restoreStock(order);

        return orderRepository.save(order);
    }

    /**
     * 确认收货
     */
    public Order confirmOrder(Long id) {
        Order order = getOrderById(id);
        User currentUser = getCurrentUser();

        // 权限检查：只有顾客可以确认收货
        if (currentUser.getRole() != UserRole.CUSTOMER || !order.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("只能确认自己的订单收货");
        }

        // 只能确认已发货的订单
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("只有已发货的订单可以确认收货");
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdateTime(LocalDateTime.now());

        return orderRepository.save(order);
    }

    /**
     * 搜索订单（管理员和商家可用）
     */
    public List<Order> searchOrders(String keyword, int page, int size) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == UserRole.CUSTOMER) {
            throw new RuntimeException("无权搜索订单");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findByOrderNumberContainingIgnoreCase(keyword, pageable);

        // 商家只能看到自己店铺的搜索结果
        if (currentUser.getRole() == UserRole.SELLER) {
            orderPage = orderRepository.findBySellerIdAndOrderNumberContainingIgnoreCase(
                    currentUser.getId(), keyword, pageable);
        }

        List<Order> orders = orderPage.getContent();
        initializeOrders(orders);
        return orders;
    }

    /**
     * 获取订单统计信息
     */
    public Map<String, Object> getOrderStats() {
        User currentUser = getCurrentUser();
        Map<String, Object> stats = new HashMap<>();

        if (currentUser.getRole() == UserRole.ADMIN) {
            // 管理员统计
            stats.put("totalOrders", orderRepository.count());
            stats.put("totalAmount", orderRepository.sumTotalAmount());
            stats.put("pendingOrders", orderRepository.countByStatus(OrderStatus.PENDING));
            stats.put("paidOrders", orderRepository.countByStatus(OrderStatus.PAID));
            stats.put("deliveredOrders", orderRepository.countByStatus(OrderStatus.DELIVERED));
            stats.put("completedOrders", orderRepository.countByStatus(OrderStatus.COMPLETED));
            stats.put("cancelledOrders", orderRepository.countByStatus(OrderStatus.CANCELLED));
        } else if (currentUser.getRole() == UserRole.SELLER) {
            // 商家统计
            Long sellerId = currentUser.getId();
            stats.put("totalOrders", orderRepository.countBySellerId(sellerId));
            stats.put("totalAmount", orderRepository.sumTotalAmountBySellerId(sellerId));
            stats.put("pendingOrders", orderRepository.countBySellerIdAndStatus(sellerId, OrderStatus.PENDING));
            stats.put("paidOrders", orderRepository.countBySellerIdAndStatus(sellerId, OrderStatus.PAID));
            stats.put("deliveredOrders", orderRepository.countBySellerIdAndStatus(sellerId, OrderStatus.DELIVERED));
            stats.put("completedOrders", orderRepository.countBySellerIdAndStatus(sellerId, OrderStatus.COMPLETED));
            stats.put("cancelledOrders", orderRepository.countBySellerIdAndStatus(sellerId, OrderStatus.CANCELLED));
        } else {
            // 顾客统计
            Long userId = currentUser.getId();
            stats.put("totalOrders", orderRepository.countByUserId(userId));
            stats.put("totalAmount", orderRepository.sumTotalAmountByUserId(userId));
            stats.put("pendingOrders", orderRepository.countByUserIdAndStatus(userId, OrderStatus.PENDING));
            stats.put("paidOrders", orderRepository.countByUserIdAndStatus(userId, OrderStatus.PAID));
            stats.put("deliveredOrders", orderRepository.countByUserIdAndStatus(userId, OrderStatus.DELIVERED));
            stats.put("completedOrders", orderRepository.countByUserIdAndStatus(userId, OrderStatus.COMPLETED));
            stats.put("cancelledOrders", orderRepository.countByUserIdAndStatus(userId, OrderStatus.CANCELLED));
        }

        return stats;
    }

    /**
     * 删除订单
     */
    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        User currentUser = getCurrentUser();

        // 删除权限检查：只有管理员和订单所属用户可以删除
        if (currentUser.getRole() != UserRole.ADMIN && !order.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("无权删除此订单");
        }

        // 只能删除已取消或已完成的订单
        if (order.getStatus() != OrderStatus.CANCELLED && order.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("只能删除已取消或已完成的订单");
        }

        // 先删除订单项
        orderItemRepository.deleteByOrderId(id);
        // 再删除订单
        orderRepository.deleteById(id);
    }

    /**
     * 批量删除订单
     */
    public void batchDeleteOrders(List<Long> orderIds) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("只有管理员可以批量删除订单");
        }

        for (Long orderId : orderIds) {
            try {
                deleteOrder(orderId);
            } catch (Exception e) {
                // 记录错误但继续处理其他订单
                System.err.println("删除订单失败: " + orderId + ", 错误: " + e.getMessage());
            }
        }
    }

    /**
     * 权限检查方法
     */
    private void checkOrderPermission(Order order) {
        User currentUser = getCurrentUser();

        // 管理员可以查看所有订单
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        // 商家只能查看自己店铺的订单
        if (currentUser.getRole() == UserRole.SELLER) {
            if (!order.getSellerId().equals(currentUser.getId())) {
                throw new RuntimeException("无权查看此订单");
            }
            return;
        }

        // 顾客只能查看自己的订单
        if (currentUser.getRole() == UserRole.CUSTOMER) {
            if (!order.getUserId().equals(currentUser.getId())) {
                throw new RuntimeException("无权查看此订单");
            }
            return;
        }

        throw new RuntimeException("用户角色无效");
    }

    /**
     * 状态流转验证
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus, UserRole userRole) {
        // 定义允许的状态转换
        Map<OrderStatus, List<OrderStatus>> allowedTransitions = Map.of(
                OrderStatus.PENDING, Arrays.asList(OrderStatus.PAID, OrderStatus.CANCELLED),
                OrderStatus.PAID, Arrays.asList(OrderStatus.DELIVERED, OrderStatus.CANCELLED),
                OrderStatus.DELIVERED, Arrays.asList(OrderStatus.COMPLETED),
                OrderStatus.COMPLETED, List.of(),
                OrderStatus.CANCELLED, List.of(),
                OrderStatus.REFUNDED, List.of()
        );

        List<OrderStatus> allowed = allowedTransitions.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new RuntimeException("不允许从状态 " + currentStatus + " 转换到 " + newStatus);
        }
    }

    /**
     * 恢复库存（取消订单时调用）
     */
    private void restoreStock(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());

        for (OrderItem item : orderItems) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在: " + item.getProductId()));
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
    }

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        Long userId = UserContext.getCurrentUserId();
        UserRole role = UserContext.getCurrentUserRole();
        String username = UserContext.getCurrentUsername();

        if (userId == null || role == null || username == null) {
            throw new RuntimeException("用户未登录");
        }

        // 创建用户对象
        User user = new User();
        user.setId(userId);
        user.setRole(role);
        user.setUsername(username);

        return user;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    /**
     * 在事务内强制初始化 orderItems 及其常用关联（防止 LazyInitializationException）
     */
    private void initializeOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) return;
        try {
            for (Order o : orders) {
                if (o == null) continue;
                if (o.getOrderItems() != null) {
                    Hibernate.initialize(o.getOrderItems());
                    o.getOrderItems().forEach(it -> {
                        try {
                            if (it.getProduct() != null) Hibernate.initialize(it.getProduct());
                        } catch (Exception ignored) {
                        }
                    });
                }
            }
        } catch (Exception ex) {
            // 记录错误但不抛出，方便 Controller 层处理
            System.err.println("初始化订单关联失败: " + ex.getMessage());
        }
    }
}