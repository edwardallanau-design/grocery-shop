package com.exam.grocery_shop.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Exception Classes Tests")
class ExceptionTest {

    @Nested
    @DisplayName("ResourceNotFoundException Tests")
    class ResourceNotFoundExceptionTests {

        @Test
        @DisplayName("Should create exception with message")
        void constructor_WithMessage_ShouldSetMessage() {
            var message = "Resource not found";
            var exception = new ResourceNotFoundException(message);

            assertAll(
                    () -> assertNotNull(exception),
                    () -> assertEquals(message, exception.getMessage()),
                    () -> assertTrue(exception instanceof RuntimeException)
            );
        }

        @Test
        @DisplayName("Should be a RuntimeException")
        void exception_ShouldBeRuntimeException() {
            var exception = new ResourceNotFoundException("Test");

            assertTrue(exception instanceof RuntimeException);
        }
    }

    @Nested
    @DisplayName("InvalidOrderException Tests")
    class InvalidOrderExceptionTests {

        @Test
        @DisplayName("Should create exception with message")
        void constructor_WithMessage_ShouldSetMessage() {
            var message = "Invalid order";
            var exception = new InvalidOrderException(message);

            assertAll(
                    () -> assertNotNull(exception),
                    () -> assertEquals(message, exception.getMessage()),
                    () -> assertTrue(exception instanceof RuntimeException)
            );
        }

        @Test
        @DisplayName("Should be a RuntimeException")
        void exception_ShouldBeRuntimeException() {
            var exception = new InvalidOrderException("Test");

            assertTrue(exception instanceof RuntimeException);
        }
    }
}
