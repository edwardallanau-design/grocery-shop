package com.exam.grocery_shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class OrderDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderRequest {
        @NotEmpty(message = "Order items cannot be empty")
        @Valid
        private List<OrderItem> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        @NotBlank(message = "Product code is required")
        private String productCode;

        @Positive(message = "Quantity must be positive")
        private Integer quantity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderResponse {
        private List<OrderLineItem> lineItems;
        private BigDecimal totalCost;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderLineItem {
        private String productCode;
        private String productName;
        private Integer totalQuantity;
        private BigDecimal totalCost;
        private List<PackageBreakdown> packages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackageBreakdown {
        private Integer packageQuantity;
        private Integer itemsPerPackage;
        private BigDecimal pricePerPackage;
        private BigDecimal subtotal;

    }
}
