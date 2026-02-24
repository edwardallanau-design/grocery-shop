package com.exam.grocery_shop.service;

import com.exam.grocery_shop.exception.InvalidOrderException;
import com.exam.grocery_shop.model.PackagingOption;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PackagingOptimizationService {

    public OptimalPackagingResult findOptimalPackaging(List<PackagingOption> options,
                                                       BigDecimal unitPrice,
                                                       int targetQuantity) {
        List<PackageInfo> availablePackages = prepareAvailablePackages(options, unitPrice);
        PackagingResult[] dpTable = initializeDPTable(targetQuantity);

        calculateOptimalPackagingForAllQuantities(dpTable, availablePackages, targetQuantity);

        return getOptimalResultOrThrow(dpTable, targetQuantity);
    }

    private List<PackageInfo> prepareAvailablePackages(List<PackagingOption> options, BigDecimal unitPrice) {
        return options.stream()
                .map(opt -> new PackageInfo(opt.getQuantity(), opt.getPackagePrice()))
                .sorted(Comparator.comparing(PackageInfo::quantity).reversed())
                .collect(Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> {
                        list.add(new PackageInfo(1, unitPrice));
                        return list;
                    }
                ));
    }

    private PackagingResult[] initializeDPTable(int targetQuantity) {
        PackagingResult[] dp = new PackagingResult[targetQuantity + 1];
        dp[0] = new PackagingResult(0, BigDecimal.ZERO, new ArrayList<>());
        return dp;
    }

    private void calculateOptimalPackagingForAllQuantities(PackagingResult[] dp,
                                                           List<PackageInfo> availablePackages,
                                                           int targetQuantity) {
        for (int currentQuantity = 1; currentQuantity <= targetQuantity; currentQuantity++) {
            dp[currentQuantity] = findBestPackagingForQuantity(dp, availablePackages, currentQuantity);
        }
    }

    private PackagingResult findBestPackagingForQuantity(PackagingResult[] dp,
                                                         List<PackageInfo> availablePackages,
                                                         int quantity) {
        PackagingResult bestResult = null;

        for (PackageInfo packageInfo : availablePackages) {
            if (canUsePackage(packageInfo, quantity, dp)) {
                PackagingResult candidateResult = createResultWithPackage(dp, packageInfo, quantity);

                if (bestResult == null || isBetterResult(candidateResult, bestResult)) {
                    bestResult = candidateResult;
                }
            }
        }

        return bestResult;
    }

    private boolean canUsePackage(PackageInfo packageInfo, int quantity, PackagingResult[] dp) {
        int remainingQuantity = quantity - packageInfo.quantity();
        return packageInfo.quantity() <= quantity && dp[remainingQuantity] != null;
    }

    private PackagingResult createResultWithPackage(PackagingResult[] dp,
                                                     PackageInfo packageInfo,
                                                     int quantity) {
        PackagingResult previousResult = dp[quantity - packageInfo.quantity()];

        int totalPackages = previousResult.totalPackages() + 1;
        BigDecimal totalCost = previousResult.totalCost().add(packageInfo.price());
        List<PackageCount> updatedPackaging = updatePackageCounts(previousResult.packaging(), packageInfo);

        return new PackagingResult(totalPackages, totalCost, updatedPackaging);
    }

    private List<PackageCount> updatePackageCounts(List<PackageCount> existingPackaging, PackageInfo newPackage) {
        List<PackageCount> updatedPackaging = new ArrayList<>(existingPackaging);

        boolean packageFound = incrementExistingPackage(updatedPackaging, newPackage);

        if (!packageFound) {
            updatedPackaging.add(new PackageCount(newPackage.quantity(), newPackage.price(), 1));
        }

        return updatedPackaging;
    }

    private boolean incrementExistingPackage(List<PackageCount> packaging, PackageInfo newPackage) {
        for (PackageCount existingPackage : packaging) {
            if (isSamePackage(existingPackage, newPackage)) {
                existingPackage.incrementCount();
                return true;
            }
        }
        return false;
    }

    private boolean isSamePackage(PackageCount existingPackage, PackageInfo newPackage) {
        return existingPackage.itemsPerPackage() == newPackage.quantity()
                && existingPackage.pricePerPackage().compareTo(newPackage.price()) == 0;
    }

    private OptimalPackagingResult getOptimalResultOrThrow(PackagingResult[] dp, int targetQuantity) {
        PackagingResult result = dp[targetQuantity];

        if (result == null) {
            throw new InvalidOrderException("Cannot fulfill order for quantity: " + targetQuantity);
        }

        return new OptimalPackagingResult(result.totalCost(), result.packaging());
    }

    private boolean isBetterResult(PackagingResult current, PackagingResult best) {
        if (current.totalPackages() != best.totalPackages()) {
            return current.totalPackages() < best.totalPackages();
        }

        return current.totalCost().compareTo(best.totalCost()) < 0;
    }

    public record PackageInfo(int quantity, BigDecimal price) {
    }

    public static final class PackageCount {
        private final int itemsPerPackage;
        private final BigDecimal pricePerPackage;
        private int count;

        public PackageCount(int itemsPerPackage, BigDecimal pricePerPackage, int count) {
            this.itemsPerPackage = itemsPerPackage;
            this.pricePerPackage = pricePerPackage;
            this.count = count;
        }

        public int itemsPerPackage() {
            return itemsPerPackage;
        }

        public BigDecimal pricePerPackage() {
            return pricePerPackage;
        }

        public int count() {
            return count;
        }

        public void incrementCount() {
            this.count++;
        }
    }

    private record PackagingResult(int totalPackages, BigDecimal totalCost, List<PackageCount> packaging) {
    }

    public record OptimalPackagingResult(BigDecimal totalCost, List<PackageCount> packaging) {
    }
}
