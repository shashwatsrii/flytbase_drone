# API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication
JWT token required for most endpoints:
```
Authorization: Bearer <token>
```

## Auth Endpoints

### POST /auth/register
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "organizationName": "Acme Inc"
}
```

### POST /auth/login
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```
Returns: `{"token": "jwt_token", "user": {...}}`

## Drone Endpoints

### GET /drones
List all drones with optional filters:
- `?status=AVAILABLE`
- `?page=0&size=20`

### GET /drones/{id}
Get specific drone

### POST /drones
```json
{
  "name": "Eagle One",
  "model": "DJI Phantom 4",
  "serialNumber": "DJI123456",
  "batteryLevel": 100
}
```

### PUT /drones/{id}
Update drone

### DELETE /drones/{id}
Delete drone

### PATCH /drones/{id}/status
```json
{"status": "AVAILABLE"}
```

## Survey Area Endpoints

### GET /survey-areas
List areas with filters:
- `?name=field`

### POST /survey-areas
```json
{
  "name": "North Field",
  "description": "Agricultural area",
  "boundaryPolygon": {
    "type": "Polygon",
    "coordinates": [[[lng, lat], ...]]
  }
}
```

### GET /survey-areas/{id}
### PUT /survey-areas/{id}
### DELETE /survey-areas/{id}

## Mission Endpoints

### GET /missions
Filters:
- `?status=IN_PROGRESS`
- `?droneId={id}`
- `?surveyAreaId={id}`

### POST /missions
```json
{
  "name": "Survey Mission",
  "droneId": "drone-uuid",
  "surveyAreaId": "area-uuid",
  "plannedStartTime": "2024-01-15T10:00:00Z",
  "flightPattern": "CROSSHATCH",
  "altitude": 100,
  "speed": 10
}
```

### POST /missions/{id}/start
### POST /missions/{id}/complete
### POST /missions/{id}/abort
### GET /missions/active

## Telemetry Endpoints

### GET /telemetry/latest/{missionId}
Get latest telemetry data

### GET /telemetry/history/{missionId}
Query parameters:
- `?startTime=2024-01-01T00:00:00Z`
- `?endTime=2024-01-02T00:00:00Z`
- `?limit=100`

### WebSocket /ws
Subscribe: `/topic/telemetry/{missionId}`

## Report Endpoints

### POST /reports/mission/{missionId}
Generate mission report

### GET /reports
Filters:
- `?type=MISSION_SUMMARY`
- `?startDate=2024-01-01`
- `?missionId={id}`

### GET /reports/{id}
### DELETE /reports/{id}

## Dashboard Endpoints

### GET /dashboard/stats
Returns organization statistics

### GET /dashboard/fleet-status
Current status of all drones

### GET /dashboard/mission-trends
- `?period=WEEKLY`
- `?startDate=2024-01-01`

## User Endpoints

### GET /users/me
Get current user profile

### PUT /users/me
Update profile

### POST /users/me/change-password
```json
{
  "currentPassword": "old123",
  "newPassword": "new456"
}
```

### GET /users (Admin only)
### POST /users (Admin only)
### PATCH /users/{id}/role (Admin only)
### DELETE /users/{id} (Admin only)

## Error Format
```json
{
  "timestamp": "2024-01-15T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/drones"
}
```

## Pagination
- `?page=0&size=20&sort=name,asc`

## WebSocket
```javascript
// Connect
const socket = new SockJS('/ws');
const client = Stomp.over(socket);

// Subscribe
client.subscribe('/topic/telemetry/{id}', callback);
client.subscribe('/topic/missions/{id}', callback);
client.subscribe('/user/queue/alerts', callback);
```