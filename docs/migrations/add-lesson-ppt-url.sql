-- Add ppt_url column to lesson table for PPT (slideshow) lesson type
-- Run: mysql -u root -p quantum_education < docs/migrations/add-lesson-ppt-url.sql

ALTER TABLE lesson ADD COLUMN ppt_url VARCHAR(500) NULL AFTER pdf_url;
