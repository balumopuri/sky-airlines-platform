# SkyReserve - Airline Reservation and Management System

Full-stack end-semester project: **React (Vite)** frontend, **Java Spring Boot** backend, **MySQL** database, REST APIs with JSON, JWT login.

## Folder structure
```
airline-project/
  backend/                      Spring Boot (Maven)
    pom.xml
    src/main/resources/application.properties
    src/main/java/com/airline/bakend/
      BakendApplication.java    starts the app
      config/DataSeeder.java    sample data + demo users
      controller/               HTTP endpoints (Auth, Catalog, Booking, Admin)
      service/                  business rules (Auth, Flight, Schedule, Booking, Jwt, Mapper)
      repository/               database access (Spring Data JPA)
      entity/                   tables as Java classes
      dto/Dtos.java             request/response shapes
      security/                 JWT filter + security rules
      exception/                clean JSON errors
  frontend/                     React app
    src/pages/                  one file per page
    src/components/             Navbar, SeatMap, ProtectedRoute, FlightSummary
    src/api.js  src/auth.jsx    API helper, login state
```

## How to run (Windows PowerShell)
Terminal 1 - backend:
```
cd D:\airline-project\backend
$env:DB_PASSWORD="your_mysql_root_password"
.\mvnw spring-boot:run
```
Wait for `Started BakendApplication` and `Sample data ready`. The first start inserts demo data (about 20-60 seconds).

Terminal 2 - frontend:
```
cd D:\airline-project\frontend
npm install
npm run dev
```
Open http://localhost:5173

## Demo accounts
| Role | Email | Password |
|---|---|---|
| Admin | admin@airline.com | admin123 |
| Customer | demo@airline.com | demo123 |

## Demo flow
1. Home -> choose DEL to BOM, a date within the next 7 days -> Search.
2. Select a flight (login if asked) -> pick seats -> enter passenger details.
3. Payment (test mode): any 16-digit card works. **A card ending in 0000 is declined** (to show failed payment). UPI: `name@bank`.
4. Ticket page shows PNR; use Print / Save as PDF.
5. My Bookings -> Cancel (allowed until 2 hours before departure) -> seat becomes available again.
6. Login as admin -> Dashboard, Flights, Schedules (block seats), Bookings, Payments.

## Database (MySQL, database `airline_db`, tables created automatically)
users, airports, airlines, aircraft, flights, flight_schedules, seats, passengers, bookings, booking_passengers, payments, tickets

Inspect data:
```
mysql -u root -p airline_db
SHOW TABLES;
SELECT id, email, role FROM users;
SELECT pnr, status, total_amount FROM bookings;
SELECT status, COUNT(*) FROM seats GROUP BY status;
SELECT booking_id, amount, status FROM payments;
```

## Key design points (for viva)
- **Layers:** Controller -> Service -> Repository -> Entity (database). DTOs are used so entities (and password hashes) never go to the browser.
- **Passwords:** hashed with BCrypt. **Login:** JWT (header.payload.signature, HMAC-SHA256), sent as `Authorization: Bearer <token>`.
- **Roles:** CUSTOMER and ADMIN. `/api/admin/**` needs ADMIN. React routes are protected too.
- **No double booking (3 layers):**
  1. `SELECT ... FOR UPDATE` row lock on the chosen seats (`@Lock(PESSIMISTIC_WRITE)`) inside one `@Transactional` method.
  2. Seat status flow `AVAILABLE -> HELD -> BOOKED`; unpaid bookings expire after 10 minutes and seats are released.
  3. Database constraints: `UNIQUE(schedule_id, seat_number)` and `UNIQUE(seat_id)` in booking_passengers; seats also have a `@Version` column.
- **Consistency:** booking = PENDING -> (payment SUCCESS) -> CONFIRMED + PNR + ticket; payment FAILED -> booking FAILED and seats released; cancel -> CANCELLED, seats released, payment REFUNDED.
- **Payment** is simulated by the server; success is only claimed when the server verifies it.
- **Secrets** are not in code: DB password and JWT secret come from environment variables (`DB_PASSWORD`, `JWT_SECRET`).

## API summary
| Method | URL | Who |
|---|---|---|
| POST | /api/auth/register, /api/auth/login | public |
| GET | /api/airports, /api/airlines | public |
| GET | /api/flights/search?from=DEL&to=BOM&date=2026-11-10&passengers=2 | public |
| GET | /api/schedules/{id}, /api/schedules/{id}/seats | public |
| GET/PUT | /api/users/me | logged in |
| POST | /api/bookings  (choose seats + passengers) | logged in |
| POST | /api/bookings/{id}/pay | owner |
| GET | /api/bookings/my, /api/bookings/{id} | owner |
| POST | /api/bookings/{id}/cancel | owner |
| GET | /api/admin/stats, /flights, /schedules, /bookings, /payments, /aircraft | admin |
| POST/PUT/DELETE | /api/admin/airports, /airlines, /aircraft, /flights | admin |
| POST | /api/admin/schedules (creates seats), PUT /api/admin/schedules/{id}/cancel | admin |
| PUT | /api/admin/seats/{id}/block?blocked=true | admin |
| POST | /api/admin/bookings/{id}/cancel | admin |
