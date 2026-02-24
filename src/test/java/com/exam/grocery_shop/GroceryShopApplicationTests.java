package com.exam.grocery_shop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("Application Context Tests")
class GroceryShopApplicationTests {

    @Test
    @DisplayName("Should load application context")
    void contextLoads() {
    }

    @Test
    @DisplayName("Should run main method successfully")
    void mainMethod_ShouldRunSuccessfully() {
        GroceryShopApplication.main(new String[]{});
    }
}
