-- SQL script to create the test database for integration tests
-- Run this script using: psql -U postgres -f create_test_db.sql

CREATE DATABASE grocerydb_test;

-- Verify the database was created
\l grocerydb_test
