package com.exam.grocery_shop.controller;

import com.exam.grocery_shop.dto.ProductDTO;
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("ProductController Integration Tests")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product with valid data")
        void createProduct_WithValidData_ShouldReturnCreated() throws Exception {
            var request = new ProductDTO.CreateProductRequest(
                    "TEST",
                    "Test Product",
                    new BigDecimal("10.00")
            );

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value("TEST"))
                    .andExpect(jsonPath("$.name").value("Test Product"))
                    .andExpect(jsonPath("$.price").value(10.00));
        }

        @Test
        @DisplayName("Should return bad request with invalid data")
        void createProduct_WithInvalidData_ShouldReturnBadRequest() throws Exception {
            var request = new ProductDTO.CreateProductRequest(
                    "",
                    "",
                    new BigDecimal("-10.00")
            );

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Get Product Tests")
    class GetProductTests {

        @Test
        @DisplayName("Should return product when it exists")
        void getProduct_WhenExists_ShouldReturnProduct() throws Exception {
            var product = Product.builder()
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
        @DisplayName("Should return all products")
        void getAllProducts_ShouldReturnAllProducts() throws Exception {
            var product1 = Product.builder()
                    .code("TEST1")
                    .name("Test Product 1")
                    .price(new BigDecimal("10.00"))
                    .build();

            var product2 = Product.builder()
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
    }

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product when it exists")
        void updateProduct_WhenExists_ShouldReturnUpdatedProduct() throws Exception {
            var product = Product.builder()
                    .code("TEST")
                    .name("Test Product")
                    .price(new BigDecimal("10.00"))
                    .build();
            productRepository.save(product);

            var request = new ProductDTO.UpdateProductRequest(
                    "Updated Product",
                    new BigDecimal("15.00")
            );

            mockMvc.perform(put("/api/products/TEST")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("TEST"))
                    .andExpect(jsonPath("$.name").value("Updated Product"))
                    .andExpect(jsonPath("$.price").value(15.00));
        }
    }

    @Nested
    @DisplayName("Delete Product Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product when it exists")
        void deleteProduct_WhenExists_ShouldReturnNoContent() throws Exception {
            var product = Product.builder()
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
    }

    @Nested
    @DisplayName("Packaging Option Tests")
    class PackagingOptionTests {

        @Test
        @DisplayName("Should add packaging option to product")
        void addPackagingOption_ShouldAddOptionToProduct() throws Exception {
            var product = Product.builder()
                    .code("TEST")
                    .name("Test Product")
                    .price(new BigDecimal("10.00"))
                    .build();
            productRepository.save(product);

            var request = new ProductDTO.PackagingOptionRequest(
                    5,
                    new BigDecimal("25.00")
            );

            mockMvc.perform(post("/api/products/TEST/packaging-options")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.packagingOptions", hasSize(1)))
                    .andExpect(jsonPath("$.packagingOptions[0].quantity").value(5))
                    .andExpect(jsonPath("$.packagingOptions[0].packagePrice").value(25.00));
        }

        @Test
        @DisplayName("Should remove packaging option when it exists")
        void removePackagingOption_WhenExists_ShouldReturnNoContent() throws Exception {
            var product = Product.builder()
                    .code("TEST")
                    .name("Test Product")
                    .price(new BigDecimal("10.00"))
                    .build();

            var packagingOption = PackagingOption.builder()
                    .quantity(5)
                    .packagePrice(new BigDecimal("25.00"))
                    .build();

            product.addPackagingOption(packagingOption);
            productRepository.save(product);

            var request = new ProductDTO.PackagingOptionRequest(
                    5,
                    new BigDecimal("25.00")
            );

            mockMvc.perform(delete("/api/products/TEST/packaging-options")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }
    }
}
