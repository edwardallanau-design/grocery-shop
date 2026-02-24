package com.exam.grocery_shop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "packaging_options")
public final class PackagingOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Package price is required")
    @Positive(message = "Package price must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal packagePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_code", nullable = false)
    @JsonIgnore
    private Product product;

    public PackagingOption() {
    }

    public PackagingOption(Long id, Integer quantity, BigDecimal packagePrice, Product product) {
        this.id = id;
        this.quantity = quantity;
        this.packagePrice = packagePrice;
        this.product = product;
    }

    public static PackagingOptionBuilder builder() {
        return new PackagingOptionBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPackagePrice() {
        return packagePrice;
    }

    public void setPackagePrice(BigDecimal packagePrice) {
        this.packagePrice = packagePrice;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PackagingOption that)) return false;
        return Objects.equals(id, that.id) &&
               Objects.equals(quantity, that.quantity) &&
               Objects.equals(packagePrice, that.packagePrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quantity, packagePrice);
    }

    public static final class PackagingOptionBuilder {
        private Long id;
        private Integer quantity;
        private BigDecimal packagePrice;
        private Product product;

        private PackagingOptionBuilder() {
        }

        public PackagingOptionBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public PackagingOptionBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public PackagingOptionBuilder packagePrice(BigDecimal packagePrice) {
            this.packagePrice = packagePrice;
            return this;
        }

        public PackagingOptionBuilder product(Product product) {
            this.product = product;
            return this;
        }

        public PackagingOption build() {
            return new PackagingOption(id, quantity, packagePrice, product);
        }
    }
}