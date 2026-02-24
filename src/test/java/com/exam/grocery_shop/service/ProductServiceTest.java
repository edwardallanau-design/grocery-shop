package com.exam.grocery_shop.service;

import com.exam.grocery_shop.dto.ProductDTO;
import com.exam.grocery_shop.exception.ResourceNotFoundException;
import com.exam.grocery_shop.mapper.ProductMapper;
import com.exam.grocery_shop.model.PackagingOption;
import com.exam.grocery_shop.model.Product;
import com.exam.grocery_shop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
final class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Spy
    private ProductMapper productMapper;

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

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void createProduct_ShouldReturnCreatedProduct() {
            var request = new ProductDTO.CreateProductRequest(
                    "TEST",
                    "Test Product",
                    new BigDecimal("10.00")
            );

            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDTO.ProductResponse response = productService.createProduct(request);

            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals("TEST", response.code()),
                    () -> assertEquals("Test Product", response.name()),
                    () -> assertEquals(new BigDecimal("10.00"), response.price())
            );
            verify(productRepository, times(1)).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Get Product Tests")
    class GetProductTests {

        @Test
        @DisplayName("Should return product when it exists")
        void getProduct_WhenProductExists_ShouldReturnProduct() {
            when(productRepository.findById("TEST")).thenReturn(Optional.of(testProduct));

            ProductDTO.ProductResponse response = productService.getProduct("TEST");

            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals("TEST", response.code())
            );
            verify(productRepository, times(1)).findById("TEST");
        }

        @Test
        @DisplayName("Should throw exception when product does not exist")
        void getProduct_WhenProductNotExists_ShouldThrowException() {
            when(productRepository.findById(anyString())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> productService.getProduct("NONEXISTENT"));
        }

        @Test
        @DisplayName("Should return all products")
        void getAllProducts_ShouldReturnAllProducts() {
            var product2 = Product.builder()
                    .code("TEST2")
                    .name("Test Product 2")
                    .price(new BigDecimal("20.00"))
                    .build();

            when(productRepository.findAll()).thenReturn(List.of(testProduct, product2));

            List<ProductDTO.ProductResponse> responses = productService.getAllProducts();

            assertAll(
                    () -> assertNotNull(responses),
                    () -> assertEquals(2, responses.size())
            );
            verify(productRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void updateProduct_ShouldUpdateAndReturnProduct() {
            var request = new ProductDTO.UpdateProductRequest(
                    "Updated Product",
                    new BigDecimal("15.00")
            );

            when(productRepository.findById("TEST")).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDTO.ProductResponse response = productService.updateProduct("TEST", request);

            assertNotNull(response);
            verify(productRepository, times(1)).findById("TEST");
            verify(productRepository, times(1)).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Delete Product Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product when it exists")
        void deleteProduct_WhenProductExists_ShouldDeleteProduct() {
            when(productRepository.findById("TEST")).thenReturn(Optional.of(testProduct));

            productService.deleteProduct("TEST");

            verify(productRepository, times(1)).findById("TEST");
            verify(productRepository, times(1)).delete(testProduct);
        }
    }

    @Nested
    @DisplayName("Packaging Option Tests")
    class PackagingOptionTests {

        @Test
        @DisplayName("Should add packaging option successfully")
        void addPackagingOption_ShouldAddOptionAndReturnProduct() {
            var request = new ProductDTO.PackagingOptionRequest(
                    5,
                    new BigDecimal("25.00")
            );

            when(productRepository.findById("TEST")).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDTO.ProductResponse response = productService.addPackagingOption("TEST", request);

            assertNotNull(response);
            verify(productRepository, times(1)).findById("TEST");
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("Should remove packaging option when it exists")
        void removePackagingOption_WhenOptionExists_ShouldRemoveOption() {
            var request = new ProductDTO.PackagingOptionRequest(
                    5,
                    new BigDecimal("25.00")
            );

            var existingOption = PackagingOption.builder()
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
        @DisplayName("Should throw exception when packaging option does not exist")
        void removePackagingOption_WhenOptionNotExists_ShouldThrowException() {
            var request = new ProductDTO.PackagingOptionRequest(
                    5,
                    new BigDecimal("25.00")
            );

            when(productRepository.findById("TEST")).thenReturn(Optional.of(testProduct));

            assertThrows(ResourceNotFoundException.class,
                    () -> productService.removePackagingOption("TEST", request));
        }
    }
}
