package com.exam.grocery_shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public final class OrderDTO {

    private OrderDTO() {
    }

    public record OrderRequest(
            @NotEmpty(message = "Order items cannot be empty") @Valid List<OrderItem> items
    ) {
    }

    public record OrderItem(
            @NotBlank(message = "Product code is required") String productCode,
            @Positive(message = "Quantity must be positive") Integer quantity
    ) {
    }

    public record OrderResponse(
            List<OrderLineItem> lineItems,
            BigDecimal totalCost
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private List<OrderLineItem> lineItems;
            private BigDecimal totalCost;

            private Builder() {
            }

            public Builder lineItems(List<OrderLineItem> lineItems) {
                this.lineItems = lineItems;
                return this;
            }

            public Builder totalCost(BigDecimal totalCost) {
                this.totalCost = totalCost;
                return this;
            }

            public OrderResponse build() {
                return new OrderResponse(lineItems, totalCost);
            }
        }
    }

    public record OrderLineItem(
            String productCode,
            String productName,
            Integer totalQuantity,
            BigDecimal totalCost,
            List<PackageBreakdown> packages
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private String productCode;
            private String productName;
            private Integer totalQuantity;
            private BigDecimal totalCost;
            private List<PackageBreakdown> packages;

            private Builder() {
            }

            public Builder productCode(String productCode) {
                this.productCode = productCode;
                return this;
            }

            public Builder productName(String productName) {
                this.productName = productName;
                return this;
            }

            public Builder totalQuantity(Integer totalQuantity) {
                this.totalQuantity = totalQuantity;
                return this;
            }

            public Builder totalCost(BigDecimal totalCost) {
                this.totalCost = totalCost;
                return this;
            }

            public Builder packages(List<PackageBreakdown> packages) {
                this.packages = packages;
                return this;
            }

            public OrderLineItem build() {
                return new OrderLineItem(productCode, productName, totalQuantity, totalCost, packages);
            }
        }
    }

    public record PackageBreakdown(
            Integer packageQuantity,
            Integer itemsPerPackage,
            BigDecimal pricePerPackage,
            BigDecimal subtotal
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private Integer packageQuantity;
            private Integer itemsPerPackage;
            private BigDecimal pricePerPackage;
            private BigDecimal subtotal;

            private Builder() {
            }

            public Builder packageQuantity(Integer packageQuantity) {
                this.packageQuantity = packageQuantity;
                return this;
            }

            public Builder itemsPerPackage(Integer itemsPerPackage) {
                this.itemsPerPackage = itemsPerPackage;
                return this;
            }

            public Builder pricePerPackage(BigDecimal pricePerPackage) {
                this.pricePerPackage = pricePerPackage;
                return this;
            }

            public Builder subtotal(BigDecimal subtotal) {
                this.subtotal = subtotal;
                return this;
            }

            public PackageBreakdown build() {
                return new PackageBreakdown(packageQuantity, itemsPerPackage, pricePerPackage, subtotal);
            }
        }
    }
}
