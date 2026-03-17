-- Add is_free column to product table for free courses support
-- Run: mysql -u root -p quantum_education < docs/migrations/add-product-is-free.sql
-- Or: mysql -u root -proot quantum_education -e "ALTER TABLE product ADD COLUMN is_free TINYINT(1) NOT NULL DEFAULT 0 AFTER is_featured;"
-- Note: If column already exists, you will get an error; that is fine.

ALTER TABLE product ADD COLUMN is_free TINYINT(1) NOT NULL DEFAULT 0 AFTER is_featured;
