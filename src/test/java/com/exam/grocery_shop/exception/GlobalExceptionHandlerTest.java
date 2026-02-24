package com.exam.grocery_shop.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("Resource Not Found Exception Tests")
    class ResourceNotFoundExceptionTests {

        @Test
        @DisplayName("Should handle ResourceNotFoundException correctly")
        void handleResourceNotFoundException_ShouldReturnNotFoundStatus() {
            var exception = new ResourceNotFoundException("Product not found");

            var response = handler.handleResourceNotFoundException(exception);

            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> assertEquals(404, response.getBody().status()),
                    () -> assertEquals("Not Found", response.getBody().error()),
                    () -> assertEquals("Product not found", response.getBody().message()),
                    () -> assertNotNull(response.getBody().timestamp())
            );
        }

        @Test
        @DisplayName("Should include timestamp in response")
        void handleResourceNotFoundException_ShouldIncludeTimestamp() {
            var exception = new ResourceNotFoundException("Resource not found");

            var response = handler.handleResourceNotFoundException(exception);

            assertNotNull(response.getBody().timestamp());
        }

        @Test
        @DisplayName("Should preserve exception message")
        void handleResourceNotFoundException_ShouldPreserveMessage() {
            var customMessage = "Custom error message";
            var exception = new ResourceNotFoundException(customMessage);

            var response = handler.handleResourceNotFoundException(exception);

            assertEquals(customMessage, response.getBody().message());
        }
    }

    @Nested
    @DisplayName("Invalid Order Exception Tests")
    class InvalidOrderExceptionTests {

        @Test
        @DisplayName("Should handle InvalidOrderException correctly")
        void handleInvalidOrderException_ShouldReturnBadRequestStatus() {
            var exception = new InvalidOrderException("Invalid order data");

            var response = handler.handleInvalidOrderException(exception);

            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> assertEquals(400, response.getBody().status()),
                    () -> assertEquals("Bad Request", response.getBody().error()),
                    () -> assertEquals("Invalid order data", response.getBody().message()),
                    () -> assertNotNull(response.getBody().timestamp())
            );
        }

        @Test
        @DisplayName("Should include timestamp in response")
        void handleInvalidOrderException_ShouldIncludeTimestamp() {
            var exception = new InvalidOrderException("Invalid order");

            var response = handler.handleInvalidOrderException(exception);

            assertNotNull(response.getBody().timestamp());
        }

        @Test
        @DisplayName("Should preserve exception message")
        void handleInvalidOrderException_ShouldPreserveMessage() {
            var customMessage = "Cannot fulfill order for quantity: 100";
            var exception = new InvalidOrderException(customMessage);

            var response = handler.handleInvalidOrderException(exception);

            assertEquals(customMessage, response.getBody().message());
        }
    }

    @Nested
    @DisplayName("Error Response Tests")
    class ErrorResponseTests {

        @Test
        @DisplayName("ErrorResponse record should store all fields correctly")
        void errorResponse_ShouldStoreAllFields() {
            var exception = new ResourceNotFoundException("Test message");
            var response = handler.handleResourceNotFoundException(exception);
            var errorResponse = response.getBody();

            assertAll(
                    () -> assertNotNull(errorResponse),
                    () -> assertNotNull(errorResponse.timestamp()),
                    () -> assertEquals(404, errorResponse.status()),
                    () -> assertEquals("Not Found", errorResponse.error()),
                    () -> assertEquals("Test message", errorResponse.message())
            );
        }
    }
}
