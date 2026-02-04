package com.exam.grocery_shop.controller;

import com.exam.grocery_shop.dto.ProductDTO;
import com.exam.grocery_shop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDTO.ProductResponse> createProduct(
            @Valid @RequestBody ProductDTO.CreateProductRequest request) {
        ProductDTO.ProductResponse response = productService.createProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{code}")
    public ResponseEntity<ProductDTO.ProductResponse> getProduct(@PathVariable String code) {
        ProductDTO.ProductResponse response = productService.getProduct(code);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO.ProductResponse>> getAllProducts() {
        List<ProductDTO.ProductResponse> responses = productService.getAllProducts();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{code}")
    public ResponseEntity<ProductDTO.ProductResponse> updateProduct(
            @PathVariable String code,
            @Valid @RequestBody ProductDTO.UpdateProductRequest request) {
        ProductDTO.ProductResponse response = productService.updateProduct(code, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String code) {
        productService.deleteProduct(code);
        return ResponseEntity.noContent().build();
    }
}
