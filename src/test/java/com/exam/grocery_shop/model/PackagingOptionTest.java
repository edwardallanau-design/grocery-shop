package com.exam.grocery_shop.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PackagingOption Model Tests")
class PackagingOptionTest {

    @Nested
    @DisplayName("Constructor and Builder Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create packaging option with builder")
        void builder_ShouldCreatePackagingOption() {
            var option = PackagingOption.builder()
                    .id(1L)
                    .quantity(5)
                    .packagePrice(new BigDecimal("25.00"))
                    .build();

            assertAll(
                    () -> assertNotNull(option),
                    () -> assertEquals(1L, option.getId()),
                    () -> assertEquals(5, option.getQuantity()),
                    () -> assertEquals(new BigDecimal("25.00"), option.getPackagePrice())
            );
        }

        @Test
        @DisplayName("Should create packaging option with no-arg constructor")
        void noArgConstructor_ShouldCreatePackagingOption() {
            var option = new PackagingOption();

            assertNotNull(option);
        }

        @Test
        @DisplayName("Should create packaging option with all-arg constructor")
        void allArgConstructor_ShouldCreatePackagingOption() {
            var product = Product.builder()
                    .code("TEST")
                    .name("Test")
                    .price(new BigDecimal("10.00"))
                    .build();

            var option = new PackagingOption(1L, 5, new BigDecimal("25.00"), product);

            assertAll(
                    () -> assertEquals(1L, option.getId()),
                    () -> assertEquals(5, option.getQuantity()),
                    () -> assertEquals(new BigDecimal("25.00"), option.getPackagePrice()),
                    () -> assertEquals(product, option.getProduct())
            );
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersSettersTests {

        @Test
        @DisplayName("Should set and get id")
        void setId_ShouldUpdateId() {
            var option = new PackagingOption();
            option.setId(10L);

            assertEquals(10L, option.getId());
        }

        @Test
        @DisplayName("Should set and get quantity")
        void setQuantity_ShouldUpdateQuantity() {
            var option = new PackagingOption();
            option.setQuantity(7);

            assertEquals(7, option.getQuantity());
        }

        @Test
        @DisplayName("Should set and get package price")
        void setPackagePrice_ShouldUpdatePrice() {
            var option = new PackagingOption();
            option.setPackagePrice(new BigDecimal("30.00"));

            assertEquals(new BigDecimal("30.00"), option.getPackagePrice());
        }

        @Test
        @DisplayName("Should set and get product")
        void setProduct_ShouldUpdateProduct() {
            var option = new PackagingOption();
            var product = Product.builder()
                    .code("TEST")
                    .name("Test")
                    .price(new BigDecimal("10.00"))
                    .build();

            option.setProduct(product);

            assertEquals(product, option.getProduct());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields match")
        void equals_SameFields_ShouldBeEqual() {
            var option1 = PackagingOption.builder()
                    .id(1L)
                    .quantity(5)
                    .packagePrice(new BigDecimal("25.00"))
                    .build();

            var option2 = PackagingOption.builder()
                    .id(1L)
                    .quantity(5)
                    .packagePrice(new BigDecimal("25.00"))
                    .build();

            assertEquals(option1, option2);
        }

        @Test
        @DisplayName("Should not be equal when fields differ")
        void equals_DifferentFields_ShouldNotBeEqual() {
            var option1 = PackagingOption.builder()
                    .id(1L)
                    .quantity(5)
                    .packagePrice(new BigDecimal("25.00"))
                    .build();

            var option2 = PackagingOption.builder()
                    .id(2L)
                    .quantity(5)
                    .packagePrice(new BigDecimal("25.00"))
                    .build();

            assertNotEquals(option1, option2);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void equals_SameInstance_ShouldBeEqual() {
            var option = PackagingOption.builder()
                    .id(1L)
                    .quantity(5)
                    .packagePrice(new BigDecimal("25.00"))
                    .build();

            assertEquals(option, option);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void equals_Null_ShouldNotBeEqual() {
            var option = PackagingOption.builder()
                    .id(1L)
                    .quantity(5)
                    .packagePrice(new BigDecimal("25.00"))
                    .build();

            assertNotEquals(null, option);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void equals_DifferentType_ShouldNotBeEqual() {
            var option = PackagingOption.builder()
                    .id(1L)
                    .quantity(5)
                    .packagePrice(new BigDecimal("25.00"))
                    .build();

            assertNotEquals(option, "string");
        }

        @Test
        @DisplayName("Should have same hashCode when equal")
        void hashCode_EqualObjects_ShouldHaveSameHashCode() {
            var option1 = PackagingOption.builder()
                    .id(1L)
                    .quantity(5)
                    .packagePrice(new BigDecimal("25.00"))
                    .build();

            var option2 = PackagingOption.builder()
                    .id(1L)
                    .quantity(5)
                    .packagePrice(new BigDecimal("25.00"))
                    .build();

            assertEquals(option1.hashCode(), option2.hashCode());
        }
    }
}
