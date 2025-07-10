# Architecture

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