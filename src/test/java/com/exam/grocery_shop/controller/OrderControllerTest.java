package com.exam.grocery_shop.controller;

import com.exam.grocery_shop.dto.OrderDTO;
import com.exam.grocery_shop.service.OrderService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController Unit Tests")
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Nested
    @DisplayName("Calculate Order Tests")
    class CalculateOrderTests {

        @Test
        @DisplayName("Should calculate order and return OK status")
        void calculateOrder_ShouldReturnOkStatus() {
            var request = new OrderDTO.OrderRequest(
                    List.of(new OrderDTO.OrderItem("TEST", 10))
            );

            var lineItem = new OrderDTO.OrderLineItem(
                    "TEST",
                    "Test Product",
                    10,
                    new BigDecimal("100.00"),
                    new ArrayList<>()
            );

            var response = new OrderDTO.OrderResponse(
                    List.of(lineItem),
                    new BigDecimal("100.00")
            );

            when(orderService.calculateOrder(request)).thenReturn(response);

            var result = orderController.calculateOrder(request);

            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(HttpStatus.OK, result.getStatusCode()),
                    () -> assertEquals(response, result.getBody()),
                    () -> assertEquals(1, result.getBody().lineItems().size()),
                    () -> assertEquals(new BigDecimal("100.00"), result.getBody().totalCost())
            );

            verify(orderService, times(1)).calculateOrder(request);
        }

        @Test
        @DisplayName("Should handle multiple items in order")
        void calculateOrder_MultipleItems_ShouldReturnAllItems() {
            var request = new OrderDTO.OrderRequest(
                    List.of(
                            new OrderDTO.OrderItem("TEST1", 5),
                            new OrderDTO.OrderItem("TEST2", 3)
                    )
            );

            var lineItems = List.of(
                    new OrderDTO.OrderLineItem("TEST1", "Product 1", 5, new BigDecimal("50.00"), new ArrayList<>()),
                    new OrderDTO.OrderLineItem("TEST2", "Product 2", 3, new BigDecimal("30.00"), new ArrayList<>())
            );

            var response = new OrderDTO.OrderResponse(
                    lineItems,
                    new BigDecimal("80.00")
            );

            when(orderService.calculateOrder(request)).thenReturn(response);

            var result = orderController.calculateOrder(request);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, result.getStatusCode()),
                    () -> assertEquals(2, result.getBody().lineItems().size()),
                    () -> assertEquals(new BigDecimal("80.00"), result.getBody().totalCost())
            );

            verify(orderService, times(1)).calculateOrder(request);
        }
    }
}
