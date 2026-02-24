package com.exam.grocery_shop.controller;

import com.exam.grocery_shop.dto.ProductDTO;
import com.exam.grocery_shop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Unit Tests")
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product and return CREATED status")
        void createProduct_ShouldReturnCreatedStatus() {
            var request = new ProductDTO.CreateProductRequest(
                    "TEST",
                    "Test Product",
                    new BigDecimal("10.00")
            );

            var response = new ProductDTO.ProductResponse(
                    "TEST",
                    "Test Product",
                    new BigDecimal("10.00"),
                    new ArrayList<>()
            );

            when(productService.createProduct(request)).thenReturn(response);

            var result = productController.createProduct(request);

            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(HttpStatus.CREATED, result.getStatusCode()),
                    () -> assertEquals(response, result.getBody())
            );

            verify(productService, times(1)).createProduct(request);
        }
    }

    @Nested
    @DisplayName("Get Product Tests")
    class GetProductTests {

        @Test
        @DisplayName("Should get product by code and return OK status")
        void getProduct_ShouldReturnOkStatus() {
            var response = new ProductDTO.ProductResponse(
                    "TEST",
                    "Test Product",
                    new BigDecimal("10.00"),
                    new ArrayList<>()
            );

            when(productService.getProduct("TEST")).thenReturn(response);

            var result = productController.getProduct("TEST");

            assertAll(
                    () -> assertEquals(HttpStatus.OK, result.getStatusCode()),
                    () -> assertEquals(response, result.getBody())
            );

            verify(productService, times(1)).getProduct("TEST");
        }

        @Test
        @DisplayName("Should get all products and return OK status")
        void getAllProducts_ShouldReturnOkStatus() {
            var products = List.of(
                    new ProductDTO.ProductResponse("TEST1", "Product 1", new BigDecimal("10.00"), new ArrayList<>()),
                    new ProductDTO.ProductResponse("TEST2", "Product 2", new BigDecimal("20.00"), new ArrayList<>())
            );

            when(productService.getAllProducts()).thenReturn(products);

            var result = productController.getAllProducts();

            assertAll(
                    () -> assertEquals(HttpStatus.OK, result.getStatusCode()),
                    () -> assertEquals(products, result.getBody()),
                    () -> assertEquals(2, result.getBody().size())
            );

            verify(productService, times(1)).getAllProducts();
        }
    }

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product and return OK status")
        void updateProduct_ShouldReturnOkStatus() {
            var request = new ProductDTO.UpdateProductRequest(
                    "Updated Product",
                    new BigDecimal("15.00")
            );

            var response = new ProductDTO.ProductResponse(
                    "TEST",
                    "Updated Product",
                    new BigDecimal("15.00"),
                    new ArrayList<>()
            );

            when(productService.updateProduct("TEST", request)).thenReturn(response);

            var result = productController.updateProduct("TEST", request);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, result.getStatusCode()),
                    () -> assertEquals(response, result.getBody())
            );

            verify(productService, times(1)).updateProduct("TEST", request);
        }
    }

    @Nested
    @DisplayName("Delete Product Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product and return NO_CONTENT status")
        void deleteProduct_ShouldReturnNoContentStatus() {
            doNothing().when(productService).deleteProduct("TEST");

            var result = productController.deleteProduct("TEST");

            assertAll(
                    () -> assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode()),
                    () -> assertNull(result.getBody())
            );

            verify(productService, times(1)).deleteProduct("TEST");
        }
    }

    @Nested
    @DisplayName("Packaging Option Tests")
    class PackagingOptionTests {

        @Test
        @DisplayName("Should add packaging option and return CREATED status")
        void addPackagingOption_ShouldReturnCreatedStatus() {
            var request = new ProductDTO.PackagingOptionRequest(
                    5,
                    new BigDecimal("25.00")
            );

            var response = new ProductDTO.ProductResponse(
                    "TEST",
                    "Test Product",
                    new BigDecimal("10.00"),
                    List.of(new ProductDTO.PackagingOptionDTO(1L, 5, new BigDecimal("25.00")))
            );

            when(productService.addPackagingOption("TEST", request)).thenReturn(response);

            var result = productController.addPackagingOption("TEST", request);

            assertAll(
                    () -> assertEquals(HttpStatus.CREATED, result.getStatusCode()),
                    () -> assertEquals(response, result.getBody()),
                    () -> assertEquals(1, result.getBody().packagingOptions().size())
            );

            verify(productService, times(1)).addPackagingOption("TEST", request);
        }

        @Test
        @DisplayName("Should remove packaging option and return NO_CONTENT status")
        void removePackagingOption_ShouldReturnNoContentStatus() {
            var request = new ProductDTO.PackagingOptionRequest(
                    5,
                    new BigDecimal("25.00")
            );

            doNothing().when(productService).removePackagingOption("TEST", request);

            var result = productController.removePackagingOption("TEST", request);

            assertAll(
                    () -> assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode()),
                    () -> assertNull(result.getBody())
            );

            verify(productService, times(1)).removePackagingOption("TEST", request);
        }
    }
}
