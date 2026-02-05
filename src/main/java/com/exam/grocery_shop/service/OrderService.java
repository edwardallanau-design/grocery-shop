package com.exam.grocery_shop.service;

import com.exam.grocery_shop.dto.OrderDTO;
import com.exam.grocery_shop.exception.InvalidOrderException;
import com.exam.grocery_shop.exception.ResourceNotFoundException;
import com.exam.grocery_shop.model.PackagingOption;
import com.exam.grocery_shop.model.Product;
import com.exam.grocery_shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public OrderDTO.OrderResponse calculateOrder(OrderDTO.OrderRequest request) {
        List<OrderDTO.OrderLineItem> lineItems = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;

        for (OrderDTO.OrderItem item : request.getItems()) {
            Product product = productRepository.findById(item.getProductCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with code: " + item.getProductCode()));

            OrderDTO.OrderLineItem lineItem = calculateLineItem(product, item.getQuantity());
            lineItems.add(lineItem);
            totalCost = totalCost.add(lineItem.getTotalCost());
        }

        return OrderDTO.OrderResponse.builder()
                .lineItems(lineItems)
                .totalCost(totalCost)
                .build();
    }

    private OrderDTO.OrderLineItem calculateLineItem(Product product, int quantity) {
        List<PackagingOption> options = new ArrayList<>(product.getPackagingOptions());

        if (options.isEmpty()) {
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

        OptimalPackaging result = findOptimalPackaging(options, product.getPrice(), quantity);

        return OrderDTO.OrderLineItem.builder()
                .productCode(product.getCode())
                .productName(product.getName())
                .totalQuantity(quantity)
                .totalCost(result.totalCost)
                .packages(result.packages)
                .build();
    }

    private OptimalPackaging findOptimalPackaging(List<PackagingOption> options,
                                                  BigDecimal unitPrice,
                                                  int targetQuantity) {

        List<PackageInfo> packageInfos = options.stream()
                .map(opt -> new PackageInfo(opt.getQuantity(), opt.getPackagePrice()))
                .sorted(Comparator.comparing(PackageInfo::getQuantity).reversed())
                .collect(Collectors.toList());

        packageInfos.add(new PackageInfo(1, unitPrice));

        PackagingResult[] dp = new PackagingResult[targetQuantity + 1];
        dp[0] = new PackagingResult(0, BigDecimal.ZERO, new ArrayList<>());

        for (int qty = 1; qty <= targetQuantity; qty++) {
            PackagingResult bestResult = null;

            for (PackageInfo pkg : packageInfos) {
                if (pkg.quantity <= qty && dp[qty - pkg.quantity] != null) {
                    PackagingResult prevResult = dp[qty - pkg.quantity];
                    int newPackages = prevResult.totalPackages + 1;
                    BigDecimal newCost = prevResult.totalCost.add(pkg.price);

                    List<PackageCount> newPackaging = new ArrayList<>(prevResult.packaging);

                    boolean found = false;
                    for (PackageCount pc : newPackaging) {
                        if (pc.itemsPerPackage == pkg.quantity && pc.pricePerPackage.compareTo(pkg.price) == 0) {
                            pc.count++;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        newPackaging.add(new PackageCount(pkg.quantity, pkg.price, 1));
                    }

                    PackagingResult currentResult = new PackagingResult(newPackages, newCost, newPackaging);

                    if (bestResult == null || isBetterResult(currentResult, bestResult)) {
                        bestResult = currentResult;
                    }
                }
            }

            dp[qty] = bestResult;
        }

        if (dp[targetQuantity] == null) {
            throw new InvalidOrderException("Cannot fulfill order for quantity: " + targetQuantity);
        }

        PackagingResult finalResult = dp[targetQuantity];
        List<OrderDTO.PackageBreakdown> packages = buildPackageBreakdown(finalResult.packaging);

        return new OptimalPackaging(finalResult.totalCost, packages);
    }

    private boolean isBetterResult(PackagingResult current, PackagingResult best) {

        if (current.totalPackages != best.totalPackages) {
            return current.totalPackages < best.totalPackages;
        }

        return current.totalCost.compareTo(best.totalCost) < 0;
    }

    private List<OrderDTO.PackageBreakdown> buildPackageBreakdown(List<PackageCount> packaging) {

        packaging.sort((a, b) -> Integer.compare(b.itemsPerPackage, a.itemsPerPackage));

        List<OrderDTO.PackageBreakdown> breakdowns = new ArrayList<>();
        for (PackageCount pc : packaging) {
            BigDecimal subtotal = pc.pricePerPackage.multiply(BigDecimal.valueOf(pc.count));
            breakdowns.add(OrderDTO.PackageBreakdown.builder()
                    .packageQuantity(pc.count)
                    .itemsPerPackage(pc.itemsPerPackage)
                    .pricePerPackage(pc.pricePerPackage)
                    .subtotal(subtotal)
                    .build());
        }

        return breakdowns;
    }


    private static class PackageInfo {
        int quantity;
        BigDecimal price;

        PackageInfo(int quantity, BigDecimal price) {
            this.quantity = quantity;
            this.price = price;
        }

        int getQuantity() {
            return quantity;
        }
    }

    private static class PackageCount {
        int itemsPerPackage;
        BigDecimal pricePerPackage;
        int count;

        PackageCount(int itemsPerPackage, BigDecimal pricePerPackage, int count) {
            this.itemsPerPackage = itemsPerPackage;
            this.pricePerPackage = pricePerPackage;
            this.count = count;
        }
    }

    private static class PackagingResult {
        int totalPackages;
        BigDecimal totalCost;
        List<PackageCount> packaging;

        PackagingResult(int totalPackages, BigDecimal totalCost, List<PackageCount> packaging) {
            this.totalPackages = totalPackages;
            this.totalCost = totalCost;
            this.packaging = packaging;
        }
    }

    private static class OptimalPackaging {
        BigDecimal totalCost;
        List<OrderDTO.PackageBreakdown> packages;

        OptimalPackaging(BigDecimal totalCost, List<OrderDTO.PackageBreakdown> packages) {
            this.totalCost = totalCost;
            this.packages = packages;
        }
    }
}
