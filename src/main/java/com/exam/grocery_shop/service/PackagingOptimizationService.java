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
        List<PackageInfo> packages = options.stream()
                .map(opt -> new PackageInfo(opt.getQuantity(), opt.getPackagePrice()))
                .sorted(Comparator.comparing(PackageInfo::getQuantity).reversed())
                .collect(Collectors.toList());

        packages.add(new PackageInfo(1, unitPrice));
        return packages;
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
        int remainingQuantity = quantity - packageInfo.quantity;
        return packageInfo.quantity <= quantity && dp[remainingQuantity] != null;
    }

    private PackagingResult createResultWithPackage(PackagingResult[] dp,
                                                     PackageInfo packageInfo,
                                                     int quantity) {
        PackagingResult previousResult = dp[quantity - packageInfo.quantity];

        int totalPackages = previousResult.totalPackages + 1;
        BigDecimal totalCost = previousResult.totalCost.add(packageInfo.price);
        List<PackageCount> updatedPackaging = updatePackageCounts(previousResult.packaging, packageInfo);

        return new PackagingResult(totalPackages, totalCost, updatedPackaging);
    }

    private List<PackageCount> updatePackageCounts(List<PackageCount> existingPackaging, PackageInfo newPackage) {
        List<PackageCount> updatedPackaging = new ArrayList<>(existingPackaging);

        boolean packageFound = incrementExistingPackage(updatedPackaging, newPackage);

        if (!packageFound) {
            updatedPackaging.add(new PackageCount(newPackage.quantity, newPackage.price, 1));
        }

        return updatedPackaging;
    }

    private boolean incrementExistingPackage(List<PackageCount> packaging, PackageInfo newPackage) {
        for (PackageCount existingPackage : packaging) {
            if (isSamePackage(existingPackage, newPackage)) {
                existingPackage.count++;
                return true;
            }
        }
        return false;
    }

    private boolean isSamePackage(PackageCount existingPackage, PackageInfo newPackage) {
        return existingPackage.itemsPerPackage == newPackage.quantity
                && existingPackage.pricePerPackage.compareTo(newPackage.price) == 0;
    }

    private OptimalPackagingResult getOptimalResultOrThrow(PackagingResult[] dp, int targetQuantity) {
        PackagingResult result = dp[targetQuantity];

        if (result == null) {
            throw new InvalidOrderException("Cannot fulfill order for quantity: " + targetQuantity);
        }

        return new OptimalPackagingResult(result.totalCost, result.packaging);
    }

    private boolean isBetterResult(PackagingResult current, PackagingResult best) {
        if (current.totalPackages != best.totalPackages) {
            return current.totalPackages < best.totalPackages;
        }

        return current.totalCost.compareTo(best.totalCost) < 0;
    }

    public static class PackageInfo {
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

    public static class PackageCount {
        int itemsPerPackage;
        BigDecimal pricePerPackage;
        int count;

        PackageCount(int itemsPerPackage, BigDecimal pricePerPackage, int count) {
            this.itemsPerPackage = itemsPerPackage;
            this.pricePerPackage = pricePerPackage;
            this.count = count;
        }

        public int getItemsPerPackage() {
            return itemsPerPackage;
        }

        public BigDecimal getPricePerPackage() {
            return pricePerPackage;
        }

        public int getCount() {
            return count;
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

    public static class OptimalPackagingResult {
        BigDecimal totalCost;
        List<PackageCount> packaging;

        OptimalPackagingResult(BigDecimal totalCost, List<PackageCount> packaging) {
            this.totalCost = totalCost;
            this.packaging = packaging;
        }

        public BigDecimal getTotalCost() {
            return totalCost;
        }

        public List<PackageCount> getPackaging() {
            return packaging;
        }
    }
}
