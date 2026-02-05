package com.exam.grocery_shop.controller;

import com.exam.grocery_shop.dto.OrderDTO;
import com.exam.grocery_shop.model.PackagingOption;
import com.exam.grocery_shop.model.Product;
import com.exam.grocery_shop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

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
    }

    @Test
    void calculateOrder_WithSampleData_ShouldReturnCorrectBreakdown() throws Exception {
        OrderDTO.OrderRequest request = OrderDTO.OrderRequest.builder()
                .items(Arrays.asList(
                        OrderDTO.OrderItem.builder().productCode("CE").quantity(10).build(),
                        OrderDTO.OrderItem.builder().productCode("HM").quantity(14).build(),
                        OrderDTO.OrderItem.builder().productCode("SS").quantity(3).build()
                ))
                .build();

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

    @Test
    void calculateOrder_WithNonExistentProduct_ShouldReturnNotFound() throws Exception {
        OrderDTO.OrderRequest request = OrderDTO.OrderRequest.builder()
                .items(Collections.singletonList(
                        OrderDTO.OrderItem.builder().productCode("INVALID").quantity(10).build()
                ))
                .build();

        mockMvc.perform(post("/api/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void calculateOrder_WithInvalidQuantity_ShouldReturnBadRequest() throws Exception {
        OrderDTO.OrderRequest request = OrderDTO.OrderRequest.builder()
                .items(Collections.singletonList(
                        OrderDTO.OrderItem.builder().productCode("CE").quantity(-5).build()
                ))
                .build();

        mockMvc.perform(post("/api/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculateOrder_WithEmptyItems_ShouldReturnBadRequest() throws Exception {
        OrderDTO.OrderRequest request = OrderDTO.OrderRequest.builder()
                .items(List.of())
                .build();

        mockMvc.perform(post("/api/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
