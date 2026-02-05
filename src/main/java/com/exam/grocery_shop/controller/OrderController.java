package com.exam.grocery_shop.controller;

import com.exam.grocery_shop.dto.OrderDTO;
import com.exam.grocery_shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/calculate")
    public ResponseEntity<OrderDTO.OrderResponse> calculateOrder(
            @Valid @RequestBody OrderDTO.OrderRequest request) {
        OrderDTO.OrderResponse response = orderService.calculateOrder(request);
        return ResponseEntity.ok(response);
    }
}
