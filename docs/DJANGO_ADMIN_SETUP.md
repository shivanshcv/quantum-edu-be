# Django Admin Panel Setup

The `quantum-edu-admin` repo provides a Django admin panel for managing courses, products, users, and other data. It connects to the same MySQL database as this Spring Boot app.

## product_category Migration (Existing Databases Only)

If your `quantum_education` database was created **before** the schema included an `id` column in `product_category`, run this migration once:

```bash
mysql -u root -p quantum_education < docs/migrations/add-product-category-id.sql
```

**When to run:**
- Existing databases with the old `product_category` (composite PK)
- Required for Django admin to manage product–category links

**When NOT to run:**
- Fresh installs using current `schema.sql` (it already has `id`)
- No Spring Boot code changes needed; this is backward compatible

## Spring Boot Compatibility

The Spring Boot app uses `product_id` and `category_id` only. The `id` column is for Django admin. No application code changes required.
