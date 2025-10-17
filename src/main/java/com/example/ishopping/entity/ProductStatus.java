package com.example.ishopping.entity;

public enum ProductStatus {
    ON_SALE("在售"),
    OFF_SALE("下架"),
    OUT_OF_STOCK("缺货"),
    DELETED("已删除");

    private final String description;

    ProductStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}