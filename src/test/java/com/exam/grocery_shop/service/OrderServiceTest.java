package com.exam.grocery_shop.service;

import com.exam.grocery_shop.dto.OrderDTO;
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
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;

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

        PackagingOption cheese3 = PackagingOption.builder()
                .id(1L)
                .quantity(3)
                .packagePrice(new BigDecimal("14.95"))
                .product(cheese)
                .build();

        PackagingOption cheese5 = PackagingOption.builder()
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

        PackagingOption ham2 = PackagingOption.builder()
                .id(3L)
                .quantity(2)
                .packagePrice(new BigDecimal("13.95"))
                .product(ham)
                .build();

        PackagingOption ham5 = PackagingOption.builder()
                .id(4L)
                .quantity(5)
                .packagePrice(new BigDecimal("29.95"))
                .product(ham)
                .build();

        PackagingOption ham8 = PackagingOption.builder()
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

    @Test
    void calculateOrder_WithSampleData_ShouldReturnCorrectBreakdown() {

        OrderDTO.OrderRequest request = OrderDTO.OrderRequest.builder()
                .items(Arrays.asList(
                        OrderDTO.OrderItem.builder().productCode("CE").quantity(10).build(),
                        OrderDTO.OrderItem.builder().productCode("HM").quantity(14).build(),
                        OrderDTO.OrderItem.builder().productCode("SS").quantity(3).build()
                ))
                .build();

        when(productRepository.findById("CE")).thenReturn(Optional.of(cheese));
        when(productRepository.findById("HM")).thenReturn(Optional.of(ham));
        when(productRepository.findById("SS")).thenReturn(Optional.of(soySauce));

        OrderDTO.OrderResponse response = orderService.calculateOrder(request);

        assertNotNull(response);
        assertEquals(3, response.getLineItems().size());

        OrderDTO.OrderLineItem cheeseItem = response.getLineItems().get(0);
        assertEquals("CE", cheeseItem.getProductCode());
        assertEquals(10, cheeseItem.getTotalQuantity());
        assertEquals(new BigDecimal("41.90"), cheeseItem.getTotalCost());
        assertEquals(1, cheeseItem.getPackages().size());
        assertEquals(2, cheeseItem.getPackages().get(0).getPackageQuantity()); // 2 packages of 5

        OrderDTO.OrderLineItem hamItem = response.getLineItems().get(1);
        assertEquals("HM", hamItem.getProductCode());
        assertEquals(14, hamItem.getTotalQuantity());
        assertEquals(new BigDecimal("78.85"), hamItem.getTotalCost());

        OrderDTO.OrderLineItem soySauceItem = response.getLineItems().get(2);
        assertEquals("SS", soySauceItem.getProductCode());
        assertEquals(3, soySauceItem.getTotalQuantity());
        assertEquals(new BigDecimal("35.85"), soySauceItem.getTotalCost());

        BigDecimal expectedTotal = new BigDecimal("41.90")
                .add(new BigDecimal("78.85"))
                .add(new BigDecimal("35.85"));
        assertEquals(expectedTotal, response.getTotalCost());
    }

    @Test
    void calculateOrder_WithNonExistentProduct_ShouldThrowException() {

        OrderDTO.OrderRequest request = OrderDTO.OrderRequest.builder()
                .items(Collections.singletonList(
                        OrderDTO.OrderItem.builder().productCode("INVALID").quantity(10).build()
                ))
                .build();

        when(productRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.calculateOrder(request);
        });
    }

    @Test
    void calculateOrder_WithNoPackagingOptions_ShouldUseUnitPrice() {

        OrderDTO.OrderRequest request = OrderDTO.OrderRequest.builder()
                .items(Collections.singletonList(
                        OrderDTO.OrderItem.builder().productCode("SS").quantity(5).build()
                ))
                .build();

        when(productRepository.findById("SS")).thenReturn(Optional.of(soySauce));

        OrderDTO.OrderResponse response = orderService.calculateOrder(request);

        assertNotNull(response);
        assertEquals(1, response.getLineItems().size());

        OrderDTO.OrderLineItem item = response.getLineItems().get(0);
        assertEquals("SS", item.getProductCode());
        assertEquals(5, item.getTotalQuantity());
        assertEquals(new BigDecimal("59.75"), item.getTotalCost()); // 5 * 11.95
        assertEquals(1, item.getPackages().size());
        assertEquals(5, item.getPackages().get(0).getPackageQuantity());
        assertEquals(1, item.getPackages().get(0).getItemsPerPackage());
    }

    @Test
    void calculateOrder_OptimalPackaging_ShouldMinimizePackageCount() {

        OrderDTO.OrderRequest request = OrderDTO.OrderRequest.builder()
                .items(Collections.singletonList(
                        OrderDTO.OrderItem.builder().productCode("CE").quantity(10).build()
                ))
                .build();

        when(productRepository.findById("CE")).thenReturn(Optional.of(cheese));

        OrderDTO.OrderResponse response = orderService.calculateOrder(request);

        OrderDTO.OrderLineItem item = response.getLineItems().get(0);

        int totalPackages = item.getPackages().stream()
                .mapToInt(OrderDTO.PackageBreakdown::getPackageQuantity)
                .sum();

        assertEquals(2, totalPackages);
    }
}
