package com.exam.grocery_shop.mapper;

import com.exam.grocery_shop.dto.ProductDTO;
import com.exam.grocery_shop.model.Product;
import org.springframework.stereotype.Component;

@Component
public final class ProductMapper {

    public ProductDTO.ProductResponse mapToResponse(Product product) {
        var options = product.getPackagingOptions().stream()
                .map(option -> ProductDTO.PackagingOptionDTO.builder()
                        .id(option.getId())
                        .quantity(option.getQuantity())
                        .packagePrice(option.getPackagePrice())
                        .build())
                .toList();

        return ProductDTO.ProductResponse.builder()
                .code(product.getCode())
                .name(product.getName())
                .price(product.getPrice())
                .packagingOptions(options)
                .build();
    }

    public Product mapToEntity(ProductDTO.CreateProductRequest request) {
        return Product.builder()
                .code(request.code())
                .name(request.name())
                .price(request.price())
                .build();
    }
}
