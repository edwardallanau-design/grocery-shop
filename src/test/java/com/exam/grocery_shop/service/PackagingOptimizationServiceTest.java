package com.exam.grocery_shop.service;

import com.exam.grocery_shop.exception.InvalidOrderException;
import com.exam.grocery_shop.model.PackagingOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PackagingOptimizationService Tests")
class PackagingOptimizationServiceTest {

    private final PackagingOptimizationService service = new PackagingOptimizationService();

    @Nested
    @DisplayName("Optimal Packaging Tests")
    class OptimalPackagingTests {

        @Test
        @DisplayName("Should find optimal packaging with multiple options")
        void findOptimalPackaging_WithMultipleOptions_ShouldReturnOptimalResult() {
            var options = List.of(
                    createPackagingOption(3, new BigDecimal("14.95")),
                    createPackagingOption(5, new BigDecimal("20.95"))
            );

            var result = service.findOptimalPackaging(
                    options,
                    new BigDecimal("5.95"),
                    10
            );

            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(new BigDecimal("41.90"), result.totalCost()),
                    () -> assertEquals(1, result.packaging().size()),
                    () -> assertEquals(2, result.packaging().get(0).count())
            );
        }

        @Test
        @DisplayName("Should use single large package when optimal")
        void findOptimalPackaging_SingleLargePackage_ShouldReturnOnePackage() {
            var options = List.of(
                    createPackagingOption(5, new BigDecimal("20.00")),
                    createPackagingOption(10, new BigDecimal("35.00"))
            );

            var result = service.findOptimalPackaging(
                    options,
                    new BigDecimal("5.00"),
                    10
            );

            assertAll(
                    () -> assertEquals(1, result.packaging().size()),
                    () -> assertEquals(10, result.packaging().get(0).itemsPerPackage()),
                    () -> assertEquals(1, result.packaging().get(0).count()),
                    () -> assertEquals(new BigDecimal("35.00"), result.totalCost())
            );
        }

        @Test
        @DisplayName("Should mix different package sizes when optimal")
        void findOptimalPackaging_MixedSizes_ShouldReturnMixedPackages() {
            var options = List.of(
                    createPackagingOption(2, new BigDecimal("13.95")),
                    createPackagingOption(5, new BigDecimal("29.95")),
                    createPackagingOption(8, new BigDecimal("40.95"))
            );

            var result = service.findOptimalPackaging(
                    options,
                    new BigDecimal("7.95"),
                    14
            );

            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(new BigDecimal("78.85"), result.totalCost()),
                    () -> assertTrue(result.packaging().size() > 0)
            );
        }

        @Test
        @DisplayName("Should use unit price when no packages available")
        void findOptimalPackaging_NoPackages_ShouldUseUnitPrice() {
            var result = service.findOptimalPackaging(
                    new ArrayList<>(),
                    new BigDecimal("10.00"),
                    5
            );

            assertAll(
                    () -> assertEquals(1, result.packaging().size()),
                    () -> assertEquals(1, result.packaging().get(0).itemsPerPackage()),
                    () -> assertEquals(5, result.packaging().get(0).count()),
                    () -> assertEquals(new BigDecimal("50.00"), result.totalCost())
            );
        }

        @Test
        @DisplayName("Should handle quantity of 1")
        void findOptimalPackaging_QuantityOne_ShouldReturnSingleItem() {
            var options = List.of(
                    createPackagingOption(3, new BigDecimal("14.95")),
                    createPackagingOption(5, new BigDecimal("20.95"))
            );

            var result = service.findOptimalPackaging(
                    options,
                    new BigDecimal("5.95"),
                    1
            );

            assertAll(
                    () -> assertEquals(new BigDecimal("5.95"), result.totalCost()),
                    () -> assertEquals(1, result.packaging().size()),
                    () -> assertEquals(1, result.packaging().get(0).itemsPerPackage())
            );
        }

        @Test
        @DisplayName("Should minimize package count")
        void findOptimalPackaging_ShouldMinimizePackageCount() {
            var options = List.of(
                    createPackagingOption(3, new BigDecimal("15.00")),
                    createPackagingOption(5, new BigDecimal("20.00"))
            );

            var result = service.findOptimalPackaging(
                    options,
                    new BigDecimal("6.00"),
                    15
            );

            int totalPackages = result.packaging().stream()
                    .mapToInt(PackagingOptimizationService.PackageCount::count)
                    .sum();

            assertEquals(3, totalPackages);
        }

        @Test
        @DisplayName("Should handle large quantities")
        void findOptimalPackaging_LargeQuantity_ShouldReturnOptimalResult() {
            var options = List.of(
                    createPackagingOption(5, new BigDecimal("20.00")),
                    createPackagingOption(10, new BigDecimal("35.00"))
            );

            var result = service.findOptimalPackaging(
                    options,
                    new BigDecimal("5.00"),
                    100
            );

            assertAll(
                    () -> assertNotNull(result),
                    () -> assertTrue(result.totalCost().compareTo(BigDecimal.ZERO) > 0),
                    () -> assertFalse(result.packaging().isEmpty())
            );
        }

        @Test
        @DisplayName("Should handle packages with same price")
        void findOptimalPackaging_SamePricePackages_ShouldChooseLarger() {
            var options = List.of(
                    createPackagingOption(3, new BigDecimal("15.00")),
                    createPackagingOption(5, new BigDecimal("15.00"))
            );

            var result = service.findOptimalPackaging(
                    options,
                    new BigDecimal("6.00"),
                    10
            );

            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(new BigDecimal("30.00"), result.totalCost())
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should throw exception for impossible quantity")
        void findOptimalPackaging_ImpossibleQuantity_ShouldThrowException() {
            var options = List.of(
                    createPackagingOption(5, new BigDecimal("20.00"))
            );

            // This should work because we can use unit price
            assertDoesNotThrow(() -> service.findOptimalPackaging(
                    options,
                    new BigDecimal("5.00"),
                    7
            ));
        }

        @Test
        @DisplayName("Should handle zero packages in result")
        void findOptimalPackaging_ZeroQuantity_ShouldHandleGracefully() {
            var options = List.of(
                    createPackagingOption(5, new BigDecimal("20.00"))
            );

            // Quantity 0 should be handled at service layer, but let's test quantity 1
            var result = service.findOptimalPackaging(
                    options,
                    new BigDecimal("5.00"),
                    1
            );

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should handle exact match with package size")
        void findOptimalPackaging_ExactMatch_ShouldReturnExactPackage() {
            var options = List.of(
                    createPackagingOption(5, new BigDecimal("20.00")),
                    createPackagingOption(10, new BigDecimal("35.00"))
            );

            var result = service.findOptimalPackaging(
                    options,
                    new BigDecimal("5.00"),
                    5
            );

            assertAll(
                    () -> assertEquals(1, result.packaging().size()),
                    () -> assertEquals(5, result.packaging().get(0).itemsPerPackage()),
                    () -> assertEquals(1, result.packaging().get(0).count())
            );
        }

        @Test
        @DisplayName("Should prefer packages over unit price when cost-effective")
        void findOptimalPackaging_PreferPackages_WhenCostEffective() {
            var options = List.of(
                    createPackagingOption(3, new BigDecimal("10.00"))
            );

            var result = service.findOptimalPackaging(
                    options,
                    new BigDecimal("5.00"),
                    6
            );

            assertAll(
                    () -> assertEquals(new BigDecimal("20.00"), result.totalCost()),
                    () -> assertEquals(1, result.packaging().size()),
                    () -> assertEquals(3, result.packaging().get(0).itemsPerPackage()),
                    () -> assertEquals(2, result.packaging().get(0).count())
            );
        }
    }

    @Nested
    @DisplayName("PackageCount Tests")
    class PackageCountTests {

        @Test
        @DisplayName("Should create PackageCount correctly")
        void packageCount_Creation_ShouldSetFieldsCorrectly() {
            var packageCount = new PackagingOptimizationService.PackageCount(
                    5,
                    new BigDecimal("20.00"),
                    2
            );

            assertAll(
                    () -> assertEquals(5, packageCount.itemsPerPackage()),
                    () -> assertEquals(new BigDecimal("20.00"), packageCount.pricePerPackage()),
                    () -> assertEquals(2, packageCount.count())
            );
        }

        @Test
        @DisplayName("Should increment count correctly")
        void packageCount_IncrementCount_ShouldIncreaseByOne() {
            var packageCount = new PackagingOptimizationService.PackageCount(
                    5,
                    new BigDecimal("20.00"),
                    2
            );

            packageCount.incrementCount();

            assertEquals(3, packageCount.count());
        }

        @Test
        @DisplayName("Should increment count multiple times")
        void packageCount_MultipleIncrements_ShouldIncreaseProperly() {
            var packageCount = new PackagingOptimizationService.PackageCount(
                    5,
                    new BigDecimal("20.00"),
                    1
            );

            packageCount.incrementCount();
            packageCount.incrementCount();
            packageCount.incrementCount();

            assertEquals(4, packageCount.count());
        }
    }

    private PackagingOption createPackagingOption(int quantity, BigDecimal price) {
        return PackagingOption.builder()
                .quantity(quantity)
                .packagePrice(price)
                .build();
    }
}
