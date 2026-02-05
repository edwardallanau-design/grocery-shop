## Technology Stack

- **Java 17**
- **Spring Boot 4.0.2**
- **Spring Data JPA**
- **H2 Database** (in-memory)
- **Maven** (build tool)
- **JUnit 5** (testing)
- **Lombok** (boilerplate reduction)

## Prerequisites

- Java Development Kit (JDK) 17 or higher
- Maven 3.6 or higher

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/edwardallanau-design/grocery-shop
cd grocery-shop-system
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Access H2 Console (Optional)

For database inspection during development:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:grocerydb`
- Username: `admin`
- Password: `admin`

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=ProductServiceTest
```

### Generate Test Coverage Report

```bash
mvn verify
```

## API Endpoints

### Product Management

#### Create Product
```http
POST /api/products
Content-Type: application/json

{
  "code": "CE",
  "name": "Cheese",
  "price": 5.95
}
```

#### Get All Products
```http
GET /api/products
```

#### Get Product by Code
```http
GET /api/products/{code}
```

#### Update Product
```http
PUT /api/products/{code}
Content-Type: application/json

{
  "name": "Updated Cheese",
  "price": 6.50
}
```

#### Delete Product
```http
DELETE /api/products/{code}
```

#### Add Packaging Option
```http
POST /api/products/{code}/packaging-options
Content-Type: application/json

{
    "quantity": 2,
    "packagePrice": 13.95
}
```

#### Delete Packaging Option
```http
POST /api/products/{code}/packaging-options
Content-Type: application/json

{
    "quantity": 2,
    "packagePrice": 13.95
}
```

### Order Calculation

#### Calculate Order
```http
POST /api/orders/calculate
Content-Type: application/json

{
  "items": [
    {
      "productCode": "CE",
      "quantity": 10
    },
    {
      "productCode": "HM",
      "quantity": 14
    },
    {
      "productCode": "SS",
      "quantity": 3
    }
  ]
}

```
