package com.exam.grocery_shop.mapper;

import com.exam.grocery_shop.dto.OrderDTO;
import com.exam.grocery_shop.model.Product;
import com.exam.grocery_shop.service.PackagingOptimizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderMapper Tests")
class OrderMapperTest {

    private OrderMapper orderMapper;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapper();
    }

    @Nested
    @DisplayName("Map to Line Item Tests")
    class MapToLineItemTests {

        @Test
        @DisplayName("Should map to line item with packaging result")
        void mapToLineItem_WithPackagingResult_ShouldMapAllFields() {
            var product = Product.builder()
                    .code("TEST")
                    .name("Test Product")
                    .price(new BigDecimal("10.00"))
                    .build();

            var packaging = List.of(
                    new PackagingOptimizationService.PackageCount(5, new BigDecimal("20.00"), 2)
            );

            var packagingResult = new PackagingOptimizationService.OptimalPackagingResult(
                    new BigDecimal("40.00"),
                    packaging
            );

            var lineItem = orderMapper.mapToLineItem(product, 10, packagingResult);

            assertAll(
                    () -> assertNotNull(lineItem),
                    () -> assertEquals("TEST", lineItem.productCode()),
                    () -> assertEquals("Test Product", lineItem.productName()),
                    () -> assertEquals(10, lineItem.totalQuantity()),
                    () -> assertEquals(new BigDecimal("40.00"), lineItem.totalCost()),
                    () -> assertEquals(1, lineItem.packages().size()),
                    () -> assertEquals(2, lineItem.packages().get(0).packageQuantity()),
                    () -> assertEquals(5, lineItem.packages().get(0).itemsPerPackage()),
                    () -> assertEquals(new BigDecimal("20.00"), lineItem.packages().get(0).pricePerPackage())
            );
        }

        @Test
        @DisplayName("Should sort packages by size descending")
        void mapToLineItem_MultiplePackages_ShouldSortBySize() {
            var product = Product.builder()
                    .code("TEST")
                    .name("Test Product")
                    .price(new BigDecimal("10.00"))
                    .build();

            var packaging = List.of(
                    new PackagingOptimizationService.PackageCount(3, new BigDecimal("15.00"), 1),
                    new PackagingOptimizationService.PackageCount(5, new BigDecimal("20.00"), 2),
                    new PackagingOptimizationService.PackageCount(1, new BigDecimal("10.00"), 3)
            );

            var packagingResult = new PackagingOptimizationService.OptimalPackagingResult(
                    new BigDecimal("85.00"),
                    packaging
            );

            var lineItem = orderMapper.mapToLineItem(product, 19, packagingResult);

            assertAll(
                    () -> assertEquals(3, lineItem.packages().size()),
                    () -> assertEquals(5, lineItem.packages().get(0).itemsPerPackage()),
                    () -> assertEquals(3, lineItem.packages().get(1).itemsPerPackage()),
                    () -> assertEquals(1, lineItem.packages().get(2).itemsPerPackage())
            );
        }

        @Test
        @DisplayName("Should calculate subtotals correctly")
        void mapToLineItem_ShouldCalculateSubtotals() {
            var product = Product.builder()
                    .code("TEST")
                    .name("Test Product")
                    .price(new BigDecimal("10.00"))
                    .build();

            var packaging = List.of(
                    new PackagingOptimizationService.PackageCount(5, new BigDecimal("20.00"), 3)
            );

            var packagingResult = new PackagingOptimizationService.OptimalPackagingResult(
                    new BigDecimal("60.00"),
                    packaging
            );

            var lineItem = orderMapper.mapToLineItem(product, 15, packagingResult);

            assertEquals(new BigDecimal("60.00"), lineItem.packages().get(0).subtotal());
        }
    }

    @Nested
    @DisplayName("Map to Line Item Without Packaging Tests")
    class MapToLineItemWithoutPackagingTests {

        @Test
        @DisplayName("Should map line item without packaging options")
        void mapToLineItemWithoutPackaging_ShouldUseUnitPrice() {
            var product = Product.builder()
                    .code("TEST")
                    .name("Test Product")
                    .price(new BigDecimal("10.00"))
                    .build();

            var lineItem = orderMapper.mapToLineItemWithoutPackaging(product, 5);

            assertAll(
                    () -> assertEquals("TEST", lineItem.productCode()),
                    () -> assertEquals("Test Product", lineItem.productName()),
                    () -> assertEquals(5, lineItem.totalQuantity()),
                    () -> assertEquals(new BigDecimal("50.00"), lineItem.totalCost()),
                    () -> assertEquals(1, lineItem.packages().size()),
                    () -> assertEquals(5, lineItem.packages().get(0).packageQuantity()),
                    () -> assertEquals(1, lineItem.packages().get(0).itemsPerPackage()),
                    () -> assertEquals(new BigDecimal("10.00"), lineItem.packages().get(0).pricePerPackage()),
                    () -> assertEquals(new BigDecimal("50.00"), lineItem.packages().get(0).subtotal())
            );
        }

        @Test
        @DisplayName("Should handle quantity of 1")
        void mapToLineItemWithoutPackaging_QuantityOne_ShouldCalculateCorrectly() {
            var product = Product.builder()
                    .code("TEST")
                    .name("Test Product")
                    .price(new BigDecimal("15.50"))
                    .build();

            var lineItem = orderMapper.mapToLineItemWithoutPackaging(product, 1);

            assertAll(
                    () -> assertEquals(new BigDecimal("15.50"), lineItem.totalCost()),
                    () -> assertEquals(1, lineItem.packages().get(0).packageQuantity())
            );
        }
    }

    @Nested
    @DisplayName("Map to Order Response Tests")
    class MapToOrderResponseTests {

        @Test
        @DisplayName("Should map order response correctly")
        void mapToOrderResponse_ShouldMapAllFields() {
            var lineItems = List.of(
                    new OrderDTO.OrderLineItem(
                            "TEST1",
                            "Product 1",
                            5,
                            new BigDecimal("25.00"),
                            new ArrayList<>()
                    ),
                    new OrderDTO.OrderLineItem(
                            "TEST2",
                            "Product 2",
                            3,
                            new BigDecimal("15.00"),
                            new ArrayList<>()
                    )
            );

            var response = orderMapper.mapToOrderResponse(lineItems, new BigDecimal("40.00"));

            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(2, response.lineItems().size()),
                    () -> assertEquals(new BigDecimal("40.00"), response.totalCost())
            );
        }

        @Test
        @DisplayName("Should handle empty line items")
        void mapToOrderResponse_EmptyLineItems_ShouldMapCorrectly() {
            var response = orderMapper.mapToOrderResponse(new ArrayList<>(), BigDecimal.ZERO);

            assertAll(
                    () -> assertNotNull(response),
                    () -> assertTrue(response.lineItems().isEmpty()),
                    () -> assertEquals(BigDecimal.ZERO, response.totalCost())
            );
        }
    }
}
