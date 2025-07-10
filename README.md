# Drone Survey Management System

A web application for managing drone operations including mission planning, real-time monitoring, and reporting.

## Features

- Mission planning with configurable flight patterns (Linear, Crosshatch, Perimeter)
- Fleet management for multiple drones
- Real-time telemetry monitoring
- Survey area definition with map drawing
- Dashboard analytics and reporting

## Tech Stack

**Backend:** Spring Boot 2.6.3, PostgreSQL, JWT Authentication
**Frontend:** React 18, TypeScript, Material-UI, Leaflet
**Real-time:** WebSocket with STOMP

## Prerequisites

- Java 11+
- Maven 3.6+
- Node.js 16+
- PostgreSQL 12+

## Setup

### Database
```bash
createdb drone_survey_db
```

### Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
Backend runs on http://localhost:8080

### Frontend
```bash
cd frontend
npm install
npm start
```
Frontend runs on http://localhost:3000

## Testing

Run the test script:
```bash
./test-system.sh
```

## Documentation

- [Architecture](./ARCHITECTURE.md)
- [API Documentation](./API_DOCUMENTATION.md)
- [User Guide](./USER_GUIDE.md)
- [Developer Guide](./DEVELOPER_GUIDE.md)

## Project Structure

```
├── src/main/java/          # Backend source
├── frontend/src/           # Frontend source
├── src/main/resources/     # Config and migrations
└── test-system.sh          # System test script
```