-- Reset Database Script
-- This script will drop all tables and recreate them with clean state
-- Run this script to reset the database after disabling seeder

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Drop all support ticket related tables
DROP TABLE IF EXISTS ticket_message;
DROP TABLE IF EXISTS support_progress;
DROP TABLE IF EXISTS support_ticket;

-- Drop all accommodation related tables
DROP TABLE IF EXISTS accommodation_review;
DROP TABLE IF EXISTS maintenance;
DROP TABLE IF EXISTS accommodation_booking;
DROP TABLE IF EXISTS room;
DROP TABLE IF EXISTS room_type;
DROP TABLE IF EXISTS property;
DROP TABLE IF EXISTS customer;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- The tables will be automatically recreated by Hibernate on next application start
-- with spring.jpa.hibernate.ddl-auto=update setting

SELECT 'Database reset completed! All tables dropped.' as message;
SELECT 'Restart the application to recreate tables with Hibernate.' as next_step;
