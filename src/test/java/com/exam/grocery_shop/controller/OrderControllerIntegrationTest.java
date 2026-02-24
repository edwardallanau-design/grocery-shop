package com.exam.grocery_shop.controller;

import com.exam.grocery_shop.dto.OrderDTO;
import com.exam.grocery_shop.model.PackagingOption;
import com.exam.grocery_shop.model.Product;
import com.exam.grocery_shop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("OrderController Integration Tests")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        var cheese = Product.builder()
                .code("CE")
                .name("Cheese")
                .price(new BigDecimal("5.95"))
                .build();

        var cheese3 = PackagingOption.builder()
                .quantity(3)
                .packagePrice(new BigDecimal("14.95"))
                .build();

        var cheese5 = PackagingOption.builder()
                .quantity(5)
                .packagePrice(new BigDecimal("20.95"))
                .build();

        cheese.addPackagingOption(cheese3);
        cheese.addPackagingOption(cheese5);
        productRepository.save(cheese);

        var ham = Product.builder()
                .code("HM")
                .name("Ham")
                .price(new BigDecimal("7.95"))
                .build();

        var ham2 = PackagingOption.builder()
                .quantity(2)
                .packagePrice(new BigDecimal("13.95"))
                .build();

        var ham5 = PackagingOption.builder()
                .quantity(5)
                .packagePrice(new BigDecimal("29.95"))
                .build();

        var ham8 = PackagingOption.builder()
                .quantity(8)
                .packagePrice(new BigDecimal("40.95"))
                .build();

        ham.addPackagingOption(ham2);
        ham.addPackagingOption(ham5);
        ham.addPackagingOption(ham8);
        productRepository.save(ham);

        var soySauce = Product.builder()
                .code("SS")
                .name("Soy Sauce")
                .price(new BigDecimal("11.95"))
                .build();
        productRepository.save(soySauce);
    }

    @Nested
    @DisplayName("Order Calculation Tests")
    class OrderCalculationTests {

        @Test
        @DisplayName("Should calculate order correctly with sample data")
        void calculateOrder_WithSampleData_ShouldReturnCorrectBreakdown() throws Exception {
            var request = new OrderDTO.OrderRequest(List.of(
                    new OrderDTO.OrderItem("CE", 10),
                    new OrderDTO.OrderItem("HM", 14),
                    new OrderDTO.OrderItem("SS", 3)
            ));

            mockMvc.perform(post("/api/orders/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.lineItems.length()").value(3))
                    .andExpect(jsonPath("$.totalCost").value(156.60))
                    .andExpect(jsonPath("$.lineItems[0].productCode").value("CE"))
                    .andExpect(jsonPath("$.lineItems[0].totalQuantity").value(10))
                    .andExpect(jsonPath("$.lineItems[0].totalCost").value(41.90))
                    .andExpect(jsonPath("$.lineItems[1].productCode").value("HM"))
                    .andExpect(jsonPath("$.lineItems[1].totalQuantity").value(14))
                    .andExpect(jsonPath("$.lineItems[1].totalCost").value(78.85))
                    .andExpect(jsonPath("$.lineItems[2].productCode").value("SS"))
                    .andExpect(jsonPath("$.lineItems[2].totalQuantity").value(3))
                    .andExpect(jsonPath("$.lineItems[2].totalCost").value(35.85));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return not found when product does not exist")
        void calculateOrder_WithNonExistentProduct_ShouldReturnNotFound() throws Exception {
            var request = new OrderDTO.OrderRequest(
                    List.of(new OrderDTO.OrderItem("INVALID", 10))
            );

            mockMvc.perform(post("/api/orders/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"));
        }

        @Test
        @DisplayName("Should return bad request with invalid quantity")
        void calculateOrder_WithInvalidQuantity_ShouldReturnBadRequest() throws Exception {
            var request = new OrderDTO.OrderRequest(
                    List.of(new OrderDTO.OrderItem("CE", -5))
            );

            mockMvc.perform(post("/api/orders/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return bad request with empty items")
        void calculateOrder_WithEmptyItems_ShouldReturnBadRequest() throws Exception {
            var request = new OrderDTO.OrderRequest(List.of());

            mockMvc.perform(post("/api/orders/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
