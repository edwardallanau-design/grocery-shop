package com.exam.grocery_shop.service;

import com.exam.grocery_shop.dto.ProductDTO;
import com.exam.grocery_shop.exception.ResourceNotFoundException;
import com.exam.grocery_shop.model.PackagingOption;
import com.exam.grocery_shop.model.Product;
import com.exam.grocery_shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductDTO.ProductResponse createProduct(ProductDTO.CreateProductRequest request) {
        Product product = Product.builder()
                .code(request.getCode())
                .name(request.getName())
                .price(request.getPrice())
                .build();

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductDTO.ProductResponse getProduct(String code) {
        Product product = findProductByCode(code);
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO.ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO.ProductResponse updateProduct(String code, ProductDTO.UpdateProductRequest request) {
        Product product = findProductByCode(code);

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteProduct(String code) {
        Product product = findProductByCode(code);
        productRepository.delete(product);
    }

    @Transactional
    public ProductDTO.ProductResponse addPackagingOption(String code, ProductDTO.PackagingOptionRequest request) {
        Product product = findProductByCode(code);

        PackagingOption option = PackagingOption.builder()
                .quantity(request.getQuantity())
                .packagePrice(request.getPackagePrice())
                .build();

        product.addPackagingOption(option);
        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    @Transactional
    public void removePackagingOption(String code, ProductDTO.PackagingOptionRequest request) {
        Product product = findProductByCode(code);

        PackagingOption option = product.getPackagingOptions().stream()
                .filter(o -> Objects.equals(o.getQuantity(), request.getQuantity()) &&
                        o.getPackagePrice().compareTo(request.getPackagePrice()) == 0)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Option not found"));

        product.removePackagingOption(option);
        productRepository.save(product);
    }

    private Product findProductByCode(String code) {
        return productRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with code: " + code));
    }

    private ProductDTO.ProductResponse mapToResponse(Product product) {
        List<ProductDTO.PackagingOptionDTO> options = product.getPackagingOptions().stream()
                .map(option -> ProductDTO.PackagingOptionDTO.builder()
                        .id(option.getId())
                        .quantity(option.getQuantity())
                        .packagePrice(option.getPackagePrice())
                        .build())
                .collect(Collectors.toList());

        return ProductDTO.ProductResponse.builder()
                .code(product.getCode())
                .name(product.getName())
                .price(product.getPrice())
                .packagingOptions(options)
                .build();
    }
}
