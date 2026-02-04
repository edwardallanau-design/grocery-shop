package com.exam.grocery_shop.service;

import com.exam.grocery_shop.dto.ProductDTO;
import com.exam.grocery_shop.exception.ResourceNotFoundException;
import com.exam.grocery_shop.model.PackagingOption;
import com.exam.grocery_shop.model.Product;
import com.exam.grocery_shop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .code("TEST")
                .name("Test Product")
                .price(new BigDecimal("10.00"))
                .packagingOptions(new ArrayList<>())
                .build();
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() {

        ProductDTO.CreateProductRequest request = ProductDTO.CreateProductRequest.builder()
                .code("TEST")
                .name("Test Product")
                .price(new BigDecimal("10.00"))
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductDTO.ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals("TEST", response.getCode());
        assertEquals("Test Product", response.getName());
        assertEquals(new BigDecimal("10.00"), response.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void getProduct_WhenProductExists_ShouldReturnProduct() {

        when(productRepository.findById("TEST")).thenReturn(Optional.of(testProduct));

        ProductDTO.ProductResponse response = productService.getProduct("TEST");

        assertNotNull(response);
        assertEquals("TEST", response.getCode());
        verify(productRepository, times(1)).findById("TEST");
    }

    @Test
    void getProduct_WhenProductNotExists_ShouldThrowException() {

        when(productRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProduct("NONEXISTENT");
        });
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {

        Product product2 = Product.builder()
                .code("TEST2")
                .name("Test Product 2")
                .price(new BigDecimal("20.00"))
                .build();

        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct, product2));

        List<ProductDTO.ProductResponse> responses = productService.getAllProducts();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void updateProduct_ShouldUpdateAndReturnProduct() {

        ProductDTO.UpdateProductRequest request = ProductDTO.UpdateProductRequest.builder()
                .name("Updated Product")
                .price(new BigDecimal("15.00"))
                .build();

        when(productRepository.findById("TEST")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductDTO.ProductResponse response = productService.updateProduct("TEST", request);

        assertNotNull(response);
        verify(productRepository, times(1)).findById("TEST");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void deleteProduct_WhenProductExists_ShouldDeleteProduct() {

        when(productRepository.findById("TEST")).thenReturn(Optional.of(testProduct));

        productService.deleteProduct("TEST");

        verify(productRepository, times(1)).findById("TEST");
        verify(productRepository, times(1)).delete(testProduct);
    }

    @Test
    void addPackagingOption_ShouldAddOptionAndReturnProduct() {
        ProductDTO.PackagingOptionRequest request = ProductDTO.PackagingOptionRequest.builder()
                .quantity(5)
                .packagePrice(new BigDecimal("25.00"))
                .build();

        when(productRepository.findById("TEST")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductDTO.ProductResponse response = productService.addPackagingOption("TEST", request);

        assertNotNull(response);
        verify(productRepository, times(1)).findById("TEST");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void removePackagingOption_WhenOptionExists_ShouldRemoveOption() {
        ProductDTO.PackagingOptionRequest request = ProductDTO.PackagingOptionRequest.builder()
                .quantity(5)
                .packagePrice(new BigDecimal("25.00"))
                .build();

        PackagingOption existingOption = PackagingOption.builder()
                .quantity(5)
                .packagePrice(new BigDecimal("25.00"))
                .build();
        testProduct.addPackagingOption(existingOption);

        when(productRepository.findById("TEST")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        productService.removePackagingOption("TEST", request);

        assertFalse(testProduct.getPackagingOptions().contains(existingOption));
        verify(productRepository).save(testProduct);
    }

    @Test
    void removePackagingOption_WhenOptionNotExists_ShouldThrowException() {

        ProductDTO.PackagingOptionRequest request = ProductDTO.PackagingOptionRequest.builder()
                .quantity(5)
                .packagePrice(new BigDecimal("25.00"))
                .build();

        when(productRepository.findById("TEST")).thenReturn(Optional.of(testProduct));

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.removePackagingOption("TEST", request);
        });
    }
}
