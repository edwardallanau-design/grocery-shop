package com.exam.grocery_shop.mapper;

import com.exam.grocery_shop.dto.ProductDTO;
import com.exam.grocery_shop.model.PackagingOption;
import com.exam.grocery_shop.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductMapper Tests")
class ProductMapperTest {

    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        productMapper = new ProductMapper();
    }

    @Nested
    @DisplayName("Map to Response Tests")
    class MapToResponseTests {

        @Test
        @DisplayName("Should map product to response correctly")
        void mapToResponse_WithProduct_ShouldMapAllFields() {
            var product = Product.builder()
                    .code("TEST")
                    .name("Test Product")
                    .price(new BigDecimal("10.00"))
                    .packagingOptions(new ArrayList<>())
                    .build();

            var response = productMapper.mapToResponse(product);

            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals("TEST", response.code()),
                    () -> assertEquals("Test Product", response.name()),
                    () -> assertEquals(new BigDecimal("10.00"), response.price()),
                    () -> assertNotNull(response.packagingOptions()),
                    () -> assertTrue(response.packagingOptions().isEmpty())
            );
        }

        @Test
        @DisplayName("Should map product with packaging options")
        void mapToResponse_WithPackagingOptions_ShouldMapOptions() {
            var product = Product.builder()
                    .code("TEST")
                    .name("Test Product")
                    .price(new BigDecimal("10.00"))
                    .packagingOptions(new ArrayList<>())
                    .build();

            var option1 = PackagingOption.builder()
                    .id(1L)
                    .quantity(5)
                    .packagePrice(new BigDecimal("25.00"))
                    .product(product)
                    .build();

            var option2 = PackagingOption.builder()
                    .id(2L)
                    .quantity(10)
                    .packagePrice(new BigDecimal("45.00"))
                    .product(product)
                    .build();

            product.getPackagingOptions().add(option1);
            product.getPackagingOptions().add(option2);

            var response = productMapper.mapToResponse(product);

            assertAll(
                    () -> assertEquals(2, response.packagingOptions().size()),
                    () -> assertEquals(1L, response.packagingOptions().get(0).id()),
                    () -> assertEquals(5, response.packagingOptions().get(0).quantity()),
                    () -> assertEquals(new BigDecimal("25.00"), response.packagingOptions().get(0).packagePrice()),
                    () -> assertEquals(2L, response.packagingOptions().get(1).id()),
                    () -> assertEquals(10, response.packagingOptions().get(1).quantity()),
                    () -> assertEquals(new BigDecimal("45.00"), response.packagingOptions().get(1).packagePrice())
            );
        }
    }

    @Nested
    @DisplayName("Map to Entity Tests")
    class MapToEntityTests {

        @Test
        @DisplayName("Should map create request to entity")
        void mapToEntity_WithCreateRequest_ShouldMapAllFields() {
            var request = new ProductDTO.CreateProductRequest(
                    "TEST",
                    "Test Product",
                    new BigDecimal("10.00")
            );

            var product = productMapper.mapToEntity(request);

            assertAll(
                    () -> assertNotNull(product),
                    () -> assertEquals("TEST", product.getCode()),
                    () -> assertEquals("Test Product", product.getName()),
                    () -> assertEquals(new BigDecimal("10.00"), product.getPrice())
            );
        }

        @Test
        @DisplayName("Should create product with empty packaging options")
        void mapToEntity_NewProduct_ShouldHaveEmptyPackagingOptions() {
            var request = new ProductDTO.CreateProductRequest(
                    "TEST",
                    "Test Product",
                    new BigDecimal("10.00")
            );

            var product = productMapper.mapToEntity(request);

            assertNotNull(product.getPackagingOptions());
        }
    }
}
