-- Create products table
CREATE TABLE IF NOT EXISTS products (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

-- Create packaging_options table
CREATE TABLE IF NOT EXISTS packaging_options (
    id BIGSERIAL PRIMARY KEY,
    quantity INTEGER NOT NULL,
    package_price DECIMAL(10, 2) NOT NULL,
    product_code VARCHAR(10) NOT NULL,
    CONSTRAINT fk_product FOREIGN KEY (product_code) REFERENCES products(code) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_packaging_product_code ON packaging_options(product_code);
