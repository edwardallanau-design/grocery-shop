# Grocery Shop API

A RESTful API for managing grocery products, packaging options, and order calculations with optimized packaging.

## Technology Stack

- **Java 25**
- **Spring Boot 4.0.2**
- **Spring Data JPA**
- **PostgreSQL Database**
- **Maven** (build tool)
- **JUnit 5** (testing)

## Features

- Product management (CRUD operations)
- Packaging options with bulk pricing
- Intelligent order calculation with optimized packaging combinations
- Full test coverage with unit and integration tests
- PostgreSQL database with automatic schema management

## Prerequisites

- Java Development Kit (JDK) 25
- Maven 3.6 or higher
- PostgreSQL 12 or higher

## Getting Started

### 1. Setup PostgreSQL Database

#### Option A: Using Docker (Recommended)

```bash
docker run --name grocery-postgres \
  -e POSTGRES_DB=grocerydb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15
```

#### Option B: Manual Installation

1. **Install PostgreSQL**
   - Download from [https://www.postgresql.org/download/](https://www.postgresql.org/download/)
   - Follow installation instructions for your operating system

2. **Create Databases**

   Using psql command-line tool:
   ```bash
   psql -U postgres
   ```

   Then run:
   ```sql
   CREATE DATABASE grocerydb;
   CREATE DATABASE grocerydb_test;
   ```

   Or using single commands:
   ```bash
   psql -U postgres -c "CREATE DATABASE grocerydb;"
   psql -U postgres -c "CREATE DATABASE grocerydb_test;"
   ```

   **Note:**
   - `grocerydb` is used for the running application
   - `grocerydb_test` is used for integration tests (with `ddl-auto=create-drop`, data is wiped after each test run)

3. **Verify Connection**
   ```bash
   psql -U postgres -d grocerydb -c "\conninfo"
   psql -U postgres -d grocerydb_test -c "\conninfo"
   ```

4. **Configure Database Credentials (Optional)**

   If you want to use different credentials, update both configuration files:

   **For the application** (`src/main/resources/application.properties`):
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/grocerydb
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

   **For tests** (`src/test/resources/application.properties`):
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/grocerydb_test
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

### 2. Clone the Repository

```bash
git clone https://github.com/edwardallanau-design/grocery-shop
cd grocery-shop
```

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

**Note:**
- The database schema will be automatically created/updated by Hibernate on first run (using `ddl-auto=update`)
- No sample data is loaded automatically - use the API endpoints to populate the database
- For initial data setup, see the [Database Seeding](#database-seeding) section below

## Running Tests

The project uses PostgreSQL for both integration tests and the running application. Integration tests use a separate `grocerydb_test` database.

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=ProductServiceTest
```

### Run Only Unit Tests (no database required)

```bash
mvn test -Dtest=*Test
```

### Run Only Integration Tests

```bash
mvn test -Dtest=*IntegrationTest
```

### Generate Test Coverage Report

```bash
mvn verify
```

**Test Database Notes:**
- Integration tests use `grocerydb_test` database
- Schema is automatically created and dropped for each test run (`ddl-auto=create-drop`)
- Tests are transactional and rollback after each test method
- No manual cleanup required

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
DELETE /api/products/{code}/packaging-options
Content-Type: application/json

{
    "quantity": 2,
    "packagePrice": 13.95
}
```

**Response Examples:**

Success (200 OK):
```json
{
  "code": "CE",
  "name": "Cheese",
  "price": 5.95,
  "packagingOptions": [
    {
      "quantity": 3,
      "packagePrice": 14.95
    },
    {
      "quantity": 5,
      "packagePrice": 20.95
    }
  ]
}
```

Error (400 Bad Request):
```json
{
  "timestamp": "2026-02-25T00:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Product code is required",
  "path": "/api/products"
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

**Response Example:**
```json
{
  "items": [
    {
      "productCode": "CE",
      "productName": "Cheese",
      "quantity": 10,
      "breakdown": [
        {
          "packageQuantity": 5,
          "numberOfPackages": 2,
          "packagePrice": 20.95,
          "subtotal": 41.90
        }
      ],
      "totalPrice": 41.90
    },
    {
      "productCode": "HM",
      "productName": "Ham",
      "quantity": 14,
      "breakdown": [
        {
          "packageQuantity": 8,
          "numberOfPackages": 1,
          "packagePrice": 40.95,
          "subtotal": 40.95
        },
        {
          "packageQuantity": 2,
          "numberOfPackages": 3,
          "packagePrice": 13.95,
          "subtotal": 41.85
        }
      ],
      "totalPrice": 82.80
    },
    {
      "productCode": "SS",
      "productName": "Soy Sauce",
      "quantity": 3,
      "breakdown": [
        {
          "packageQuantity": 1,
          "numberOfPackages": 3,
          "packagePrice": 11.95,
          "subtotal": 35.85
        }
      ],
      "totalPrice": 35.85
    }
  ],
  "totalOrderPrice": 160.55
}
```

## Database Seeding

To populate the database with sample data, use the API endpoints. Here's an example using `curl`:

### Create Sample Products

```bash
# Create Cheese product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "code": "CE",
    "name": "Cheese",
    "price": 5.95
  }'

# Add packaging options to Cheese
curl -X POST http://localhost:8080/api/products/CE/packaging-options \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 3,
    "packagePrice": 14.95
  }'

curl -X POST http://localhost:8080/api/products/CE/packaging-options \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 5,
    "packagePrice": 20.95
  }'

# Create Ham product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "code": "HM",
    "name": "Ham",
    "price": 7.95
  }'

# Add packaging options to Ham
curl -X POST http://localhost:8080/api/products/HM/packaging-options \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 2,
    "packagePrice": 13.95
  }'

curl -X POST http://localhost:8080/api/products/HM/packaging-options \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 5,
    "packagePrice": 29.95
  }'

curl -X POST http://localhost:8080/api/products/HM/packaging-options \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 8,
    "packagePrice": 40.95
  }'

# Create Soy Sauce product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "code": "SS",
    "name": "Soy Sauce",
    "price": 11.95
  }'
```

## Database Schema

The application automatically creates the following tables:

### `products`
| Column | Type | Constraints |
|--------|------|-------------|
| code | VARCHAR(10) | PRIMARY KEY |
| name | VARCHAR(255) | NOT NULL |
| price | NUMERIC(10,2) | NOT NULL |

### `packaging_options`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| product_code | VARCHAR(10) | FOREIGN KEY → products(code) |
| quantity | INTEGER | NOT NULL |
| package_price | NUMERIC(10,2) | NOT NULL |

## Configuration

### Application Configuration

Edit `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/grocerydb
spring.datasource.username=postgres
spring.datasource.password=postgres

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

### Test Configuration

Edit `src/test/resources/application.properties`:

```properties
# Test Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/grocerydb_test
spring.datasource.username=postgres
spring.datasource.password=postgres

# JPA Configuration for Tests
spring.jpa.hibernate.ddl-auto=create-drop
```

## Project Structure

```
grocery-shop/
├── src/
│   ├── main/
│   │   ├── java/com/exam/grocery_shop/
│   │   │   ├── controller/         # REST controllers
│   │   │   ├── dto/                # Data Transfer Objects
│   │   │   ├── exception/          # Exception handlers
│   │   │   ├── mapper/             # DTO mappers
│   │   │   ├── model/              # JPA entities
│   │   │   ├── repository/         # Spring Data repositories
│   │   │   ├── service/            # Business logic
│   │   │   └── GroceryShopApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── schema.sql          # Optional manual schema
│   └── test/
│       ├── java/com/exam/grocery_shop/
│       │   ├── controller/         # Controller tests
│       │   ├── exception/          # Exception tests
│       │   ├── mapper/             # Mapper tests
│       │   ├── model/              # Model tests
│       │   └── service/            # Service tests
│       └── resources/
│           └── application.properties
├── pom.xml
└── README.md
```

## Troubleshooting

### Database Connection Issues

If you encounter database connection errors:

1. Verify PostgreSQL is running:
   ```bash
   psql -U postgres -c "SELECT version();"
   ```

2. Check if databases exist:
   ```bash
   psql -U postgres -c "\l"
   ```

3. Create missing databases:
   ```bash
   psql -U postgres -c "CREATE DATABASE grocerydb;"
   psql -U postgres -c "CREATE DATABASE grocerydb_test;"
   ```

### Port Already in Use

If port 8080 is already in use, change it in `application.properties`:
```properties
server.port=8081
```

### Test Failures

If integration tests fail:
1. Ensure `grocerydb_test` database exists
2. Verify database credentials in `src/test/resources/application.properties`
3. Check PostgreSQL is accessible on `localhost:5432`

## License

This project is licensed under the MIT License.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
