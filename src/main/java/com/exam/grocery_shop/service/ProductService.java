package com.exam.grocery_shop.service;

import com.exam.grocery_shop.dto.ProductDTO;
import com.exam.grocery_shop.exception.ResourceNotFoundException;
import com.exam.grocery_shop.mapper.ProductMapper;
import com.exam.grocery_shop.model.PackagingOption;
import com.exam.grocery_shop.model.Product;
import com.exam.grocery_shop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductDTO.ProductResponse createProduct(ProductDTO.CreateProductRequest request) {
        Product product = productMapper.mapToEntity(request);
        Product saved = productRepository.save(product);
        return productMapper.mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductDTO.ProductResponse getProduct(String code) {
        Product product = findProductByCode(code);
        return productMapper.mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO.ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::mapToResponse)
                .toList();
    }

    @Transactional
    public ProductDTO.ProductResponse updateProduct(String code, ProductDTO.UpdateProductRequest request) {
        Product product = findProductByCode(code);

        Optional.ofNullable(request.name()).ifPresent(product::setName);
        Optional.ofNullable(request.price()).ifPresent(product::setPrice);

        Product updated = productRepository.save(product);
        return productMapper.mapToResponse(updated);
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
                .quantity(request.quantity())
                .packagePrice(request.packagePrice())
                .build();

        product.addPackagingOption(option);
        Product updated = productRepository.save(product);
        return productMapper.mapToResponse(updated);
    }

    @Transactional
    public void removePackagingOption(String code, ProductDTO.PackagingOptionRequest request) {
        Product product = findProductByCode(code);

        PackagingOption option = product.getPackagingOptions().stream()
                .filter(o -> isMatchingOption(o, request))
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

    private boolean isMatchingOption(PackagingOption o, ProductDTO.PackagingOptionRequest req) {
        return Objects.equals(o.getQuantity(), req.quantity()) &&
                o.getPackagePrice().compareTo(req.packagePrice()) == 0;
    }
}
