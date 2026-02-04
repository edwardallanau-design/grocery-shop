package com.exam.grocery_shop.controller;

import com.exam.grocery_shop.dto.ProductDTO;
import com.exam.grocery_shop.model.PackagingOption;
import com.exam.grocery_shop.model.Product;
import com.exam.grocery_shop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private tools.jackson.databind.ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void createProduct_WithValidData_ShouldReturnCreated() throws Exception {
        ProductDTO.CreateProductRequest request = ProductDTO.CreateProductRequest.builder()
                .code("TEST")
                .name("Test Product")
                .price(new BigDecimal("10.00"))
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("TEST"))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(10.00));
    }

    @Test
    void createProduct_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        ProductDTO.CreateProductRequest request = ProductDTO.CreateProductRequest.builder()
                .code("")
                .name("")
                .price(new BigDecimal("-10.00"))
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProduct_WhenExists_ShouldReturnProduct() throws Exception {
        Product product = Product.builder()
                .code("TEST")
                .name("Test Product")
                .price(new BigDecimal("10.00"))
                .build();
        productRepository.save(product);

        mockMvc.perform(get("/api/products/TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TEST"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() throws Exception {
        Product product1 = Product.builder()
                .code("TEST1")
                .name("Test Product 1")
                .price(new BigDecimal("10.00"))
                .build();

        Product product2 = Product.builder()
                .code("TEST2")
                .name("Test Product 2")
                .price(new BigDecimal("20.00"))
                .build();

        productRepository.save(product1);
        productRepository.save(product2);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].code", containsInAnyOrder("TEST1", "TEST2")));
    }

    @Test
    void updateProduct_WhenExists_ShouldReturnUpdatedProduct() throws Exception {
        Product product = Product.builder()
                .code("TEST")
                .name("Test Product")
                .price(new BigDecimal("10.00"))
                .build();
        productRepository.save(product);

        ProductDTO.UpdateProductRequest request = ProductDTO.UpdateProductRequest.builder()
                .name("Updated Product")
                .price(new BigDecimal("15.00"))
                .build();

        mockMvc.perform(put("/api/products/TEST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TEST"))
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price").value(15.00));
    }

    @Test
    void deleteProduct_WhenExists_ShouldReturnNoContent() throws Exception {
        Product product = Product.builder()
                .code("TEST")
                .name("Test Product")
                .price(new BigDecimal("10.00"))
                .build();
        productRepository.save(product);

        mockMvc.perform(delete("/api/products/TEST"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/TEST"))
                .andExpect(status().isNotFound());

    }

    @Test
    void addPackagingOption_ShouldAddOptionToProduct() throws Exception {
        Product product = Product.builder()
                .code("TEST")
                .name("Test Product")
                .price(new BigDecimal("10.00"))
                .build();
        productRepository.save(product);

        ProductDTO.PackagingOptionRequest request = ProductDTO.PackagingOptionRequest.builder()
                .quantity(5)
                .packagePrice(new BigDecimal("25.00"))
                .build();

        mockMvc.perform(post("/api/products/TEST/packaging-options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.packagingOptions", hasSize(1)))
                .andExpect(jsonPath("$.packagingOptions[0].quantity").value(5))
                .andExpect(jsonPath("$.packagingOptions[0].packagePrice").value(25.00));
    }

    @Test
    void removePackagingOption_WhenExists_ShouldReturnNoContent() throws Exception {

        Product product = Product.builder()
                .code("TEST")
                .name("Test Product")
                .price(new BigDecimal("10.00"))
                .build();

        ProductDTO.PackagingOptionRequest request = ProductDTO.PackagingOptionRequest.builder()
                .quantity(5)
                .packagePrice(new BigDecimal("25.00"))
                .build();

        PackagingOption packagingOption = PackagingOption.builder()
                        .quantity(5)
                        .packagePrice(new BigDecimal("25.00"))
                        .build();

        product.addPackagingOption(packagingOption);
        productRepository.save(product);

        mockMvc.perform(delete("/api/products/TEST/packaging-options") // Removed trailing slash
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

    }
}
