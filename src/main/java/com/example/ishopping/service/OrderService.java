package com.example.ishopping.service;

import com.example.ishopping.dto.CreateOrderRequest;
import com.example.ishopping.dto.OrderItemRequest;
import com.example.ishopping.entity.*;
import com.example.ishopping.repository.OrderRepository;
import com.example.ishopping.repository.ProductRepository;
import com.example.ishopping.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在: " + itemRequest.getProductId()));

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new RuntimeException("商品库存不足: " + product.getName());
            }

            // 更新库存
            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice());

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(product.getPrice().multiply(
                    BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setCreateTime(LocalDateTime.now());

        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    public Order cancelOrder(Long id) {
        Order order = getOrderById(id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("只有待支付的订单可以取消");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis() +
                String.format("%04d", new Random().nextInt(10000));
    }
}