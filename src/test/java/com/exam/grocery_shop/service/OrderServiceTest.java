package com.exam.grocery_shop.service;

import com.exam.grocery_shop.dto.OrderDTO;
import com.exam.grocery_shop.exception.ResourceNotFoundException;
import com.exam.grocery_shop.mapper.OrderMapper;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
final class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Spy
    private PackagingOptimizationService packagingOptimizationService;

    @Spy
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Product cheese;
    private Product ham;
    private Product soySauce;

    @BeforeEach
    void setUp() {
        cheese = Product.builder()
                .code("CE")
                .name("Cheese")
                .price(new BigDecimal("5.95"))
                .packagingOptions(new ArrayList<>())
                .build();

        var cheese3 = PackagingOption.builder()
                .id(1L)
                .quantity(3)
                .packagePrice(new BigDecimal("14.95"))
                .product(cheese)
                .build();

        var cheese5 = PackagingOption.builder()
                .id(2L)
                .quantity(5)
                .packagePrice(new BigDecimal("20.95"))
                .product(cheese)
                .build();

        cheese.getPackagingOptions().add(cheese3);
        cheese.getPackagingOptions().add(cheese5);

        ham = Product.builder()
                .code("HM")
                .name("Ham")
                .price(new BigDecimal("7.95"))
                .packagingOptions(new ArrayList<>())
                .build();

        var ham2 = PackagingOption.builder()
                .id(3L)
                .quantity(2)
                .packagePrice(new BigDecimal("13.95"))
                .product(ham)
                .build();

        var ham5 = PackagingOption.builder()
                .id(4L)
                .quantity(5)
                .packagePrice(new BigDecimal("29.95"))
                .product(ham)
                .build();

        var ham8 = PackagingOption.builder()
                .id(5L)
                .quantity(8)
                .packagePrice(new BigDecimal("40.95"))
                .product(ham)
                .build();

        ham.getPackagingOptions().add(ham2);
        ham.getPackagingOptions().add(ham5);
        ham.getPackagingOptions().add(ham8);

        soySauce = Product.builder()
                .code("SS")
                .name("Soy Sauce")
                .price(new BigDecimal("11.95"))
                .packagingOptions(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("Order Calculation Tests")
    class OrderCalculationTests {

        @Test
        @DisplayName("Should calculate order correctly with sample data")
        void calculateOrder_WithSampleData_ShouldReturnCorrectBreakdown() {
            var request = new OrderDTO.OrderRequest(List.of(
                    new OrderDTO.OrderItem("CE", 10),
                    new OrderDTO.OrderItem("HM", 14),
                    new OrderDTO.OrderItem("SS", 3)
            ));

            when(productRepository.findById("CE")).thenReturn(Optional.of(cheese));
            when(productRepository.findById("HM")).thenReturn(Optional.of(ham));
            when(productRepository.findById("SS")).thenReturn(Optional.of(soySauce));

            OrderDTO.OrderResponse response = orderService.calculateOrder(request);

            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(3, response.lineItems().size())
            );

            OrderDTO.OrderLineItem cheeseItem = response.lineItems().get(0);
            assertAll(
                    () -> assertEquals("CE", cheeseItem.productCode()),
                    () -> assertEquals(10, cheeseItem.totalQuantity()),
                    () -> assertEquals(new BigDecimal("41.90"), cheeseItem.totalCost()),
                    () -> assertEquals(1, cheeseItem.packages().size()),
                    () -> assertEquals(2, cheeseItem.packages().get(0).packageQuantity())
            );

            OrderDTO.OrderLineItem hamItem = response.lineItems().get(1);
            assertAll(
                    () -> assertEquals("HM", hamItem.productCode()),
                    () -> assertEquals(14, hamItem.totalQuantity()),
                    () -> assertEquals(new BigDecimal("78.85"), hamItem.totalCost())
            );

            OrderDTO.OrderLineItem soySauceItem = response.lineItems().get(2);
            assertAll(
                    () -> assertEquals("SS", soySauceItem.productCode()),
                    () -> assertEquals(3, soySauceItem.totalQuantity()),
                    () -> assertEquals(new BigDecimal("35.85"), soySauceItem.totalCost())
            );

            var expectedTotal = new BigDecimal("41.90")
                    .add(new BigDecimal("78.85"))
                    .add(new BigDecimal("35.85"));
            assertEquals(expectedTotal, response.totalCost());
        }

        @Test
        @DisplayName("Should use unit price when no packaging options available")
        void calculateOrder_WithNoPackagingOptions_ShouldUseUnitPrice() {
            var request = new OrderDTO.OrderRequest(
                    List.of(new OrderDTO.OrderItem("SS", 5))
            );

            when(productRepository.findById("SS")).thenReturn(Optional.of(soySauce));

            OrderDTO.OrderResponse response = orderService.calculateOrder(request);

            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(1, response.lineItems().size())
            );

            OrderDTO.OrderLineItem item = response.lineItems().get(0);
            assertAll(
                    () -> assertEquals("SS", item.productCode()),
                    () -> assertEquals(5, item.totalQuantity()),
                    () -> assertEquals(new BigDecimal("59.75"), item.totalCost()),
                    () -> assertEquals(1, item.packages().size()),
                    () -> assertEquals(5, item.packages().get(0).packageQuantity()),
                    () -> assertEquals(1, item.packages().get(0).itemsPerPackage())
            );
        }

        @Test
        @DisplayName("Should minimize package count in optimal packaging")
        void calculateOrder_OptimalPackaging_ShouldMinimizePackageCount() {
            var request = new OrderDTO.OrderRequest(
                    List.of(new OrderDTO.OrderItem("CE", 10))
            );

            when(productRepository.findById("CE")).thenReturn(Optional.of(cheese));

            OrderDTO.OrderResponse response = orderService.calculateOrder(request);

            OrderDTO.OrderLineItem item = response.lineItems().get(0);

            int totalPackages = item.packages().stream()
                    .mapToInt(OrderDTO.PackageBreakdown::packageQuantity)
                    .sum();

            assertEquals(2, totalPackages);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw exception when product does not exist")
        void calculateOrder_WithNonExistentProduct_ShouldThrowException() {
            var request = new OrderDTO.OrderRequest(
                    List.of(new OrderDTO.OrderItem("INVALID", 10))
            );

            when(productRepository.findById(anyString())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> orderService.calculateOrder(request));
        }
    }
}
