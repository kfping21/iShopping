package com.example.ishopping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public class CreateOrderRequest {

    @NotBlank(message = "收货地址不能为空")
    @Size(max = 200, message = "收货地址不能超过200个字符")
    private String shippingAddress;

    @NotBlank(message = "收货人姓名不能为空")
    @Size(max = 50, message = "收货人姓名不能超过50个字符")
    private String receiverName;

    @NotBlank(message = "收货人电话不能为空")
    @Size(max = 20, message = "收货人电话不能超过20个字符")
    private String receiverPhone;

    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    private BigDecimal discountAmount = BigDecimal.ZERO;

    private BigDecimal shippingFee = BigDecimal.ZERO;

    @NotNull(message = "订单项不能为空")
    @Size(min = 1, message = "至少需要一个订单项")
    private List<OrderItemRequest> orderItems;

    // Getter 和 Setter 方法
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }

    public List<OrderItemRequest> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemRequest> orderItems) { this.orderItems = orderItems; }

    public static class OrderItemRequest {
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @NotNull(message = "商品数量不能为空")
        private Integer quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}