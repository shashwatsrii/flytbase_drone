in# Architecture

## Technologies Used

### Backend
- **Java 11** - Core programming language
- **Spring Boot 2.6.3** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database ORM
- **Spring WebSocket** - Real-time communication
- **JWT (JSON Web Tokens)** - Stateless authentication
- **Maven** - Build and dependency management
- **Lombok** - Boilerplate code reduction
- **PostgreSQL 12+** - Primary database with spatial support
- **Flyway** - Database migration management
- **Jackson** - JSON processing
- **SLF4J + Logback** - Logging framework

### Frontend
- **React 18** - UI framework
- **TypeScript** - Type-safe JavaScript
- **Redux Toolkit** - State management
- **React Router v6** - Client-side routing
- **Material-UI (MUI) v5** - Component library
- **Ant Design** - Additional UI components
- **Leaflet** - Interactive maps
- **React-Leaflet** - React wrapper for Leaflet
- **Axios** - HTTP client
- **STOMP.js** - WebSocket client
- **Chart.js** - Data visualization
- **React-Chartjs-2** - React wrapper for Chart.js
- **date-fns** - Date utility library

### Development & Build Tools
- **Node.js 16+** - JavaScript runtime
- **npm** - Package manager
- **Create React App** - React build setup
- **ESLint** - JavaScript linting
- **Prettier** - Code formatting

### Testing
- **JUnit 5** - Java unit testing
- **Mockito** - Java mocking framework
- **Jest** - JavaScript testing (configured in CRA)
- **React Testing Library** - React component testing

### Infrastructure
- **WebSocket (STOMP protocol)** - Real-time communication
- **REST API** - Client-server communication
- **JWT** - API authentication
- **CORS** - Cross-origin resource sharing

## Overview

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Frontend      │     │   Backend API   │     │   PostgreSQL    │
│   (React)       │────▶│  (Spring Boot)  │────▶│   Database      │
│                 │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                        │
        │                        │
        ▼                        ▼
┌─────────────────┐     ┌─────────────────┐
│   WebSocket     │     │   Scheduled     │
│   (Real-time)   │     │   Tasks         │
└─────────────────┘     └─────────────────┘
```

## Components

### Frontend
- React with TypeScript
- Redux for state management
- Leaflet for maps
- Material-UI components

### Backend
- Spring Boot REST API
- JWT authentication
- WebSocket for real-time updates
- Flyway for database migrations

### Database
- PostgreSQL
- 20+ tables for comprehensive data model
- Materialized views for dashboard statistics

## Key Design Decisions

1. **Monolithic Architecture**: Simple deployment, easy to maintain
2. **JWT Authentication**: Stateless, scalable authentication
3. **WebSocket**: Real-time telemetry updates
4. **PostgreSQL**: Reliable, supports spatial data
5. **Materialized Views**: Optimized dashboard queries

## Data Flow

1. User interacts with React frontend
2. Frontend makes API calls to Spring Boot backend
3. Backend validates requests and interacts with database
4. Real-time updates sent via WebSocket
5. Dashboard statistics refreshed every 5 minutes

## Database Schema

```
┌──────────────────┐
│  organizations   │
│ ─────────────── │
│ • id (PK)       │
│ • name          │
│ • created_at    │
└──────────────────┘
         │
         │ 1:N
         ▼
┌──────────────────┐     ┌──────────────────┐
│     users        │     │     drones       │
│ ─────────────── │     │ ─────────────── │
│ • id (PK)       │     │ • id (PK)       │
│ • email         │     │ • name          │
│ • full_name     │     │ • model         │
│ • password_hash │     │ • serial_number │
│ • role          │     │ • status        │
│ • org_id (FK)   │     │ • battery_level │
│ • created_at    │     │ • org_id (FK)   │
└──────────────────┘     └──────────────────┘
         │                        │
         │ 1:N                    │ 1:N
         ▼                        ▼
┌──────────────────┐     ┌──────────────────┐
│  survey_areas    │     │    missions      │
│ ─────────────── │     │ ─────────────── │
│ • id (PK)       │     │ • id (PK)       │
│ • name          │ ────│ • name          │
│ • description   │ 1:N │ • status        │
│ • boundary      │     │ • drone_id (FK) │
│ • area          │     │ • area_id (FK)  │
│ • org_id (FK)   │     │ • created_by    │
│ • created_by    │     │ • start_time    │
└──────────────────┘     │ • end_time      │
                         └──────────────────┘
                                  │
                                  │ 1:N
                                  ▼
                         ┌──────────────────┐
                         │ drone_telemetry  │
                         │ ─────────────── │
                         │ • id (PK)       │
                         │ • mission_id    │
                         │ • drone_id      │
                         │ • timestamp     │
                         │ • gps_latitude  │
                         │ • gps_longitude │
                         │ • altitude      │
                         │ • battery_level │
                         │ • telemetry_data│
                         └──────────────────┘

Additional Tables:
• mission_waypoints - Stores generated flight paths
• mission_alerts - Tracks anomalies and warnings
• mission_progress_cache - Optimized progress tracking
• drone_maintenance_log - Maintenance history
• audit_log - User action tracking
• reports - Generated mission reports
• dashboard_stats_view - Materialized view for analytics
```

## Security

- JWT tokens with 24-hour expiration
- Role-based access control (ADMIN, OPERATOR, VIEWER)
- Organization-level data isolation
- Input validation on all endpoints