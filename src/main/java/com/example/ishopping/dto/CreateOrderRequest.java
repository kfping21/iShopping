package com.example.ishopping.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "订单项不能为空")
    private List<OrderItemRequest> items;
}