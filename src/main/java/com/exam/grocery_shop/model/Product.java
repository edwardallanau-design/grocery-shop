package com.exam.grocery_shop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name ="products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

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
    @Builder.Default
    private List<PackagingOption> packagingOptions = new ArrayList<>();

    public void addPackagingOption(PackagingOption option) {
        packagingOptions.add(option);
        option.setProduct(this);
    }

    public void removePackagingOption(PackagingOption option) {
        packagingOptions.remove(option);
        option.setProduct(null);
    }

}
