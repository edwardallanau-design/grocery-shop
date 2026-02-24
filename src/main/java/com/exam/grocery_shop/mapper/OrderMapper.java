package com.exam.grocery_shop.mapper;

import com.exam.grocery_shop.dto.OrderDTO;
import com.exam.grocery_shop.model.Product;
import com.exam.grocery_shop.service.PackagingOptimizationService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class OrderMapper {

    public OrderDTO.OrderLineItem mapToLineItem(Product product,
                                                 int quantity,
                                                 PackagingOptimizationService.OptimalPackagingResult packagingResult) {
        List<OrderDTO.PackageBreakdown> packages = buildPackageBreakdown(packagingResult.getPackaging());

        return OrderDTO.OrderLineItem.builder()
                .productCode(product.getCode())
                .productName(product.getName())
                .totalQuantity(quantity)
                .totalCost(packagingResult.getTotalCost())
                .packages(packages)
                .build();
    }

    public OrderDTO.OrderLineItem mapToLineItemWithoutPackaging(Product product, int quantity) {
        BigDecimal lineCost = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        OrderDTO.PackageBreakdown breakdown = OrderDTO.PackageBreakdown.builder()
                .packageQuantity(quantity)
                .itemsPerPackage(1)
                .pricePerPackage(product.getPrice())
                .subtotal(lineCost)
                .build();

        return OrderDTO.OrderLineItem.builder()
                .productCode(product.getCode())
                .productName(product.getName())
                .totalQuantity(quantity)
                .totalCost(lineCost)
                .packages(Collections.singletonList(breakdown))
                .build();
    }

    public OrderDTO.OrderResponse mapToOrderResponse(List<OrderDTO.OrderLineItem> lineItems, BigDecimal totalCost) {
        return OrderDTO.OrderResponse.builder()
                .lineItems(lineItems)
                .totalCost(totalCost)
                .build();
    }

    private List<OrderDTO.PackageBreakdown> buildPackageBreakdown(List<PackagingOptimizationService.PackageCount> packaging) {
        List<PackagingOptimizationService.PackageCount> sortedPackaging = new ArrayList<>(packaging);
        sortedPackaging.sort((a, b) -> Integer.compare(b.getItemsPerPackage(), a.getItemsPerPackage()));

        List<OrderDTO.PackageBreakdown> breakdowns = new ArrayList<>();
        for (PackagingOptimizationService.PackageCount pc : sortedPackaging) {
            BigDecimal subtotal = pc.getPricePerPackage().multiply(BigDecimal.valueOf(pc.getCount()));
            breakdowns.add(OrderDTO.PackageBreakdown.builder()
                    .packageQuantity(pc.getCount())
                    .itemsPerPackage(pc.getItemsPerPackage())
                    .pricePerPackage(pc.getPricePerPackage())
                    .subtotal(subtotal)
                    .build());
        }

        return breakdowns;
    }
}
