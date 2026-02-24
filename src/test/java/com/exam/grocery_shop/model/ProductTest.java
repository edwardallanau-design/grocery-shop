package com.exam.grocery_shop.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Product Model Tests")
class ProductTest {

    @Nested
    @DisplayName("Constructor and Builder Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create product with builder")
        void builder_ShouldCreateProduct() {
            var product = Product.builder()
                    .code("TEST")
                    .name("Test Product")
                    .price(new BigDecimal("10.00"))
                    .build();

            assertAll(
                    () -> assertNotNull(product),
                    () -> assertEquals("TEST", product.getCode()),
                    () -> assertEquals("Test Product", product.getName()),
                    () -> assertEquals(new BigDecimal("10.00"), product.getPrice()),
                    () -> assertNotNull(product.getPackagingOptions())
            );
        }

        @Test
        @DisplayName("Should create product with no-arg constructor")
        void noArgConstructor_ShouldCreateProduct() {
            var product = new Product();

            assertNotNull(product);
        }

        @Test
        @DisplayName("Should create product with all-arg constructor")
        void allArgConstructor_ShouldCreateProduct() {
            var options = new ArrayList<PackagingOption>();
            var product = new Product("TEST", "Test Product", new BigDecimal("10.00"), options);

            assertAll(
                    () -> assertEquals("TEST", product.getCode()),
                    () -> assertEquals("Test Product", product.getName()),
                    () -> assertEquals(new BigDecimal("10.00"), product.getPrice()),
                    () -> assertNotNull(product.getPackagingOptions())
            );
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersSettersTests {

        @Test
        @DisplayName("Should set and get code")
        void setCode_ShouldUpdateCode() {
            var product = new Product();
            product.setCode("NEW");

            assertEquals("NEW", product.getCode());
        }

        @Test
        @DisplayName("Should set and get name")
        void setName_ShouldUpdateName() {
            var product = new Product();
            product.setName("New Name");

            assertEquals("New Name", product.getName());
        }

        @Test
        @DisplayName("Should set and get price")
        void setPrice_ShouldUpdatePrice() {
            var product = new Product();
            product.setPrice(new BigDecimal("20.00"));

            assertEquals(new BigDecimal("20.00"), product.getPrice());
        }

        @Test
        @DisplayName("Should set and get packaging options")
        void setPackagingOptions_ShouldUpdateOptions() {
            var product = new Product();
            var options = new ArrayList<PackagingOption>();
            product.setPackagingOptions(options);

            assertSame(options, product.getPackagingOptions());
        }
    }

    @Nested
    @DisplayName("Packaging Option Management Tests")
    class PackagingOptionManagementTests {

        @Test
        @DisplayName("Should add packaging option")
        void addPackagingOption_ShouldAddToList() {
            var product = Product.builder()
                    .code("TEST")
                    .name("Test Product")
                    .price(new BigDecimal("10.00"))
                    .build();

            var option = PackagingOption.builder()
                    .quantity(5)
                    .packagePrice(new BigDecimal("25.00"))
                    .build();

            product.addPackagingOption(option);

            assertAll(
                    () -> assertEquals(1, product.getPackagingOptions().size()),
                    () -> assertTrue(product.getPackagingOptions().contains(option)),
                    () -> assertEquals(product, option.getProduct())
            );
        }

        @Test
        @DisplayName("Should remove packaging option")
        void removePackagingOption_ShouldRemoveFromList() {
            var product = Product.builder()
                    .code("TEST")
                    .name("Test Product")
                    .price(new BigDecimal("10.00"))
                    .build();

            var option = PackagingOption.builder()
                    .quantity(5)
                    .packagePrice(new BigDecimal("25.00"))
                    .build();

            product.addPackagingOption(option);
            assertEquals(1, product.getPackagingOptions().size());

            product.removePackagingOption(option);

            assertAll(
                    () -> assertEquals(0, product.getPackagingOptions().size()),
                    () -> assertFalse(product.getPackagingOptions().contains(option)),
                    () -> assertNull(option.getProduct())
            );
        }

        @Test
        @DisplayName("Should add multiple packaging options")
        void addPackagingOption_Multiple_ShouldAddAll() {
            var product = Product.builder()
                    .code("TEST")
                    .name("Test Product")
                    .price(new BigDecimal("10.00"))
                    .build();

            var option1 = PackagingOption.builder()
                    .quantity(3)
                    .packagePrice(new BigDecimal("15.00"))
                    .build();

            var option2 = PackagingOption.builder()
                    .quantity(5)
                    .packagePrice(new BigDecimal("25.00"))
                    .build();

            product.addPackagingOption(option1);
            product.addPackagingOption(option2);

            assertAll(
                    () -> assertEquals(2, product.getPackagingOptions().size()),
                    () -> assertTrue(product.getPackagingOptions().contains(option1)),
                    () -> assertTrue(product.getPackagingOptions().contains(option2))
            );
        }
    }
}
