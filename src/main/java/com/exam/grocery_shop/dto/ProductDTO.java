package com.exam.grocery_shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public final class ProductDTO {

    private ProductDTO() {
    }

    public record CreateProductRequest(
            @NotBlank(message = "Product code is required") String code,
            @NotBlank(message = "Product name is required") String name,
            @NotNull(message = "Price is required") @Positive(message = "Price must be positive") BigDecimal price
    ) {
    }

    public record UpdateProductRequest(
            String name,
            BigDecimal price
    ) {
    }

    public record ProductResponse(
            String code,
            String name,
            BigDecimal price,
            List<PackagingOptionDTO> packagingOptions
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private String code;
            private String name;
            private BigDecimal price;
            private List<PackagingOptionDTO> packagingOptions;

            private Builder() {
            }

            public Builder code(String code) {
                this.code = code;
                return this;
            }

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder price(BigDecimal price) {
                this.price = price;
                return this;
            }

            public Builder packagingOptions(List<PackagingOptionDTO> packagingOptions) {
                this.packagingOptions = packagingOptions;
                return this;
            }

            public ProductResponse build() {
                return new ProductResponse(code, name, price, packagingOptions);
            }
        }
    }

    public record PackagingOptionDTO(
            Long id,
            Integer quantity,
            BigDecimal packagePrice
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private Long id;
            private Integer quantity;
            private BigDecimal packagePrice;

            private Builder() {
            }

            public Builder id(Long id) {
                this.id = id;
                return this;
            }

            public Builder quantity(Integer quantity) {
                this.quantity = quantity;
                return this;
            }

            public Builder packagePrice(BigDecimal packagePrice) {
                this.packagePrice = packagePrice;
                return this;
            }

            public PackagingOptionDTO build() {
                return new PackagingOptionDTO(id, quantity, packagePrice);
            }
        }
    }

    public record PackagingOptionRequest(
            @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") Integer quantity,
            @NotNull(message = "Package price is required") @Positive(message = "Package price must be positive") BigDecimal packagePrice
    ) {
    }
}
