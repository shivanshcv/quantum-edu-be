-- Add billing_name column to user_profile for separate profile vs billing name
-- Run: mysql -u root -p quantum_education < docs/migrations/add-user-profile-billing-name.sql

ALTER TABLE user_profile ADD COLUMN billing_name VARCHAR(255) AFTER last_name;
