# Vehicle Service Center Management (Java Swing + MySQL)

Same stack as the student-management-system project, adapted for a vehicle service center.

## Features
- Login screen (default: admin / admin123)
- Customers, Vehicles, Service Jobs: add, update, delete, search
- Vehicle photo field with local image path selection
- Service status: Pending / In Progress / Completed / Delivered
- Deleting a customer removes their vehicles and jobs (cascade)

## Setup
1. Install JDK 21 (LTS), Maven, MySQL.
2. Run `sql/schema.sql` in MySQL.
3. Set your MySQL username/password in `src/main/java/vsc/DB.java`.
4. Run: `mvn compile exec:java`

Tip: add customers first, then vehicles (use the Customer ID), then services (use the Vehicle ID).
