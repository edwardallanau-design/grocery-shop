package com.exam.grocery_shop.config;

import com.exam.grocery_shop.model.PackagingOption;
import com.exam.grocery_shop.model.Product;
import com.exam.grocery_shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing sample data...");

        Product cheese = Product.builder()
                .code("CE")
                .name("Cheese")
                .price(new BigDecimal("5.95"))
                .build();

        PackagingOption cheese3 = PackagingOption.builder()
                .quantity(3)
                .packagePrice(new BigDecimal("14.95"))
                .build();

        PackagingOption cheese5 = PackagingOption.builder()
                .quantity(5)
                .packagePrice(new BigDecimal("20.95"))
                .build();

        cheese.addPackagingOption(cheese3);
        cheese.addPackagingOption(cheese5);
        productRepository.save(cheese);

        Product ham = Product.builder()
                .code("HM")
                .name("Ham")
                .price(new BigDecimal("7.95"))
                .build();

        PackagingOption ham2 = PackagingOption.builder()
                .quantity(2)
                .packagePrice(new BigDecimal("13.95"))
                .build();

        PackagingOption ham5 = PackagingOption.builder()
                .quantity(5)
                .packagePrice(new BigDecimal("29.95"))
                .build();

        PackagingOption ham8 = PackagingOption.builder()
                .quantity(8)
                .packagePrice(new BigDecimal("40.95"))
                .build();

        ham.addPackagingOption(ham2);
        ham.addPackagingOption(ham5);
        ham.addPackagingOption(ham8);
        productRepository.save(ham);

        Product soySauce = Product.builder()
                .code("SS")
                .name("Soy Sauce")
                .price(new BigDecimal("11.95"))
                .build();

        productRepository.save(soySauce);

        log.info("Sample data initialization completed.");
        log.info("Created products: CE (Cheese), HM (Ham), SS (Soy Sauce)");
    }
}
