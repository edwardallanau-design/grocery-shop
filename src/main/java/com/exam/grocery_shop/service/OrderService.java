package com.exam.grocery_shop.service;

import com.exam.grocery_shop.dto.OrderDTO;
import com.exam.grocery_shop.exception.ResourceNotFoundException;
import com.exam.grocery_shop.mapper.OrderMapper;
import com.exam.grocery_shop.model.Product;
import com.exam.grocery_shop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final PackagingOptimizationService packagingOptimizationService;
    private final OrderMapper orderMapper;

    public OrderService(ProductRepository productRepository,
                        PackagingOptimizationService packagingOptimizationService,
                        OrderMapper orderMapper) {
        this.productRepository = productRepository;
        this.packagingOptimizationService = packagingOptimizationService;
        this.orderMapper = orderMapper;
    }

    @Transactional(readOnly = true)
    public OrderDTO.OrderResponse calculateOrder(OrderDTO.OrderRequest request) {
        List<OrderDTO.OrderLineItem> lineItems = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;

        for (OrderDTO.OrderItem item : request.items()) {
            Product product = findProductByCode(item.productCode());

            OrderDTO.OrderLineItem lineItem = calculateLineItem(product, item.quantity());
            lineItems.add(lineItem);
            totalCost = totalCost.add(lineItem.totalCost());
        }

        return orderMapper.mapToOrderResponse(lineItems, totalCost);
    }

    private Product findProductByCode(String productCode) {
        return productRepository.findById(productCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with code: " + productCode));
    }

    private OrderDTO.OrderLineItem calculateLineItem(Product product, int quantity) {
        if (product.getPackagingOptions().isEmpty()) {
            return orderMapper.mapToLineItemWithoutPackaging(product, quantity);
        }

        PackagingOptimizationService.OptimalPackagingResult packagingResult =
                packagingOptimizationService.findOptimalPackaging(
                        product.getPackagingOptions(),
                        product.getPrice(),
                        quantity
                );

        return orderMapper.mapToLineItem(product, quantity, packagingResult);
    }
}
