package com.exam.grocery_shop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public final class Product {

    @Id
    @NotBlank(message = "Product code is required")
    @Column(length = 10, unique = true)
    private String code;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PackagingOption> packagingOptions = new ArrayList<>();

    public Product() {
    }

    public Product(String code, String name, BigDecimal price, List<PackagingOption> packagingOptions) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.packagingOptions = packagingOptions != null ? new ArrayList<>(packagingOptions) : new ArrayList<>();
    }

    public static ProductBuilder builder() {
        return new ProductBuilder();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<PackagingOption> getPackagingOptions() {
        return packagingOptions;
    }

    public void setPackagingOptions(List<PackagingOption> packagingOptions) {
        this.packagingOptions = packagingOptions;
    }

    public void addPackagingOption(PackagingOption option) {
        packagingOptions.add(option);
        option.setProduct(this);
    }

    public void removePackagingOption(PackagingOption option) {
        packagingOptions.remove(option);
        option.setProduct(null);
    }

    public static final class ProductBuilder {
        private String code;
        private String name;
        private BigDecimal price;
        private List<PackagingOption> packagingOptions = new ArrayList<>();

        private ProductBuilder() {
        }

        public ProductBuilder code(String code) {
            this.code = code;
            return this;
        }

        public ProductBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ProductBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public ProductBuilder packagingOptions(List<PackagingOption> packagingOptions) {
            this.packagingOptions = packagingOptions;
            return this;
        }

        public Product build() {
            return new Product(code, name, price, packagingOptions);
        }
    }
}
