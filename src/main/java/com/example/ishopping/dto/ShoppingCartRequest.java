package com.example.ishopping.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class ShoppingCartRequest {
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "数量不能为空")
    private Integer quantity;
}