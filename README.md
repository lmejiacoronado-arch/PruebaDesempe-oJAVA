HotelNova — Hotel Management System

Sistema de gestión de hospedaje desarrollado en Java SE 17, utilizando JDBC puro, arquitectura en capas tipo MVC, y múltiples interfaces de usuario (Console, Swing, JavaFX).

Diseñado para demostrar buenas prácticas de:

Arquitectura limpia
Separación de responsabilidades
Manejo de transacciones
Testing con mocks
Persistencia sin ORM
Overview

HotelNova permite:

Gestión de habitaciones
Gestión de huéspedes
Reservas con validaciones complejas
Check-in / Check-out con transacciones
Exportación de reportes CSV
Architecture
View → Controller → Service → DAO → Database
View: interacción con el usuario (3 implementaciones)
Controller: orquestación de flujos
Service: lógica de negocio + transacciones
DAO: acceso a datos con JDBC
Database: PostgreSQL
Tech Stack
Layer	Technology
Language	Java 17
Build	Maven
Database	PostgreSQL (Docker)
Persistence	JDBC (no ORM)
UI	Console / Swing / JavaFX
Testing	JUnit 5 + Mockito
Logging	java.util.logging
Security	SHA-256 hashing
Quick Start
1. Run PostgreSQL (Docker)
   docker run --name prueba \
   -e POSTGRES_USER=prueba \
   -e POSTGRES_PASSWORD=prueba \
   -e POSTGRES_DB=prueba \
   -p 5433:5432 \
   -v postgres_data:/var/lib/postgresql/data \
   -d postgres:15

Verify:

docker ps
2. Initialize Database
   cat src/main/resources/schema.sql | docker exec -i -e PGPASSWORD=prueba prueba psql -U prueba -d prueba
3. Configure Application

Edit:

src/main/resources/config.properties
db.url=jdbc:postgresql://localhost:5433/prueba
db.user=prueba
db.password=prueba
view.type=swing
hotel.tax.rate=0.19
4. Run Application
   mvn compile exec:java -Dexec.mainClass="com.hotelnova.Main"
5. Default Access
   User	Password
   admin	admin123

SHA-256:

240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
Running Tests

Run all tests:

mvn test

Run specific class:

mvn -Dtest=ReservationServiceTest test

Run single method:

mvn -Dtest=ReservationServiceTest#shouldThrowWhenRoomNotAvailable test
Testing Strategy
Unit tests focused on Service layer
DAO layer is mocked using Mockito
No dependency on database
Covers:
business rules validation
reservation conflicts
cost calculation
edge cases
Business Rules
Rule	Description
BR-001	Unique room number
BR-002	Room must be available
BR-003	Guest must be active
BR-004	Valid date range
BR-005	No overlapping reservations
BR-006	Cannot checkout inactive reservation
Transactions

Handled in ReservationService.

Guarantee:

Atomic operations: commit or rollback

Ensures consistency between:

reservations
room status
Exception Design
AppException
├── ValidationException
├── NotFoundException
├── DuplicateException
├── RoomNotAvailableException
├── ReservationConflictException
├── CheckoutException
└── DatabaseException
Project Structure
src/
├── main/java/com/hotelnova/
│   ├── controller/
│   ├── service/
│   ├── dao/
│   ├── dao/impl/
│   ├── model/entity/
│   ├── view/
│   ├── config/
│   ├── db/
│   └── util/
├── resources/
└── test/
Design Decisions
No ORM → full control over SQL
Constructor injection → testability
Service layer owns business logic
DAO strictly for persistence
Multiple UI implementations (polymorphism)
External configuration
Logs

Location:

logs/app.log
rotation enabled
max 3 files
2MB each
Common Issues
Database connection fails
docker ps
Port already in use

Change:

-p 5434:5432
Tables not found

Run:

schema.sql
Why This Project Matters

This project demonstrates:

real backend architecture (not CRUD básico)
transaction management
clean layering
unit testing with isolation
scalability-ready design
Future Improvements
REST API (Spring Boot)
Authentication with JWT
Docker Compose (app + DB)
CI/CD pipeline
Integration tests
Author

Luis Mejia
Software Engineering