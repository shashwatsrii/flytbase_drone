in # Developer Guide

## Contents
1. [Setup](#setup)
2. [Structure](#structure)
3. [Development](#development)
4. [Testing](#testing)
5. [Deployment](#deployment)

## Setup

### Requirements
- Java 11+
- Maven 3.6+
- Node.js 16+
- PostgreSQL 12+

### Quick Start
```bash
# Database
createdb drone_survey_db

# Backend
cd backend
mvn spring-boot:run

# Frontend
cd frontend
npm install
npm start
```

## Structure

```
backend/
├── controller/   # REST APIs
├── service/      # Business logic
├── repository/   # Data access
├── entity/       # Database models
└── dto/          # Data transfer

frontend/
├── pages/        # Page components
├── components/   # Reusable UI
├── services/     # API calls
└── store/        # State management
```

## Development

### Code Style
- Java: Google Java Format (`mvn fmt:format`)
- TypeScript: ESLint + Prettier
- Commits: Conventional format (feat, fix, docs)

### Workflow
1. Create feature branch
2. Write code and tests
3. Format code
4. Submit PR
5. Merge after review

### Adding APIs

1. Create DTO with validation
2. Add service method with business logic
3. Create controller endpoint
4. Add to API documentation

Example:
```java
@PostMapping("/api/drones")
public ResponseEntity<DroneResponse> createDrone(@Valid @RequestBody DroneRequest request) {
    return ResponseEntity.ok(droneService.create(request));
}
```

### Adding UI Components

1. Define TypeScript types
2. Create API service
3. Build React component
4. Add to page

Example:
```typescript
export const DroneList: React.FC = () => {
  const [drones, setDrones] = useState<Drone[]>([]);
  
  useEffect(() => {
    droneService.getAll().then(setDrones);
  }, []);
  
  return <Table dataSource={drones} />;
};
```

### Database

**Migrations**: `V{number}__description.sql`

**Best Practices**:
- Add indexes for foreign keys
- Use JSONB for flexible data
- Create materialized views for stats
- Test migrations locally first

## Testing

### Backend
```bash
mvn test                    # Run all tests
mvn test -Dtest=DroneTest   # Run specific test
```

### Frontend
```bash
npm test                    # Run all tests
npm test -- --coverage      # With coverage
```

### System Test
```bash
./test-system.sh            # Test all endpoints
```

## Deployment

### Development
```bash
mvn spring-boot:run    # Backend on :8080
npm start              # Frontend on :3000
```

### Production
```bash
# Build
mvn clean package
npm run build

# Run
java -jar target/*.jar

# Environment
DB_HOST=prod-db.example.com
DB_USER=drone_user
DB_PASSWORD=***
JWT_SECRET=***
```

## Troubleshooting

**Backend issues**: Check database, Java version, port 8080
**Frontend issues**: Clear node_modules, check Node version
**Migration issues**: Check naming, SQL syntax

## Best Practices

- Use environment variables for secrets
- Add indexes for performance
- Paginate large lists
- Validate all inputs
- Keep methods small
- Update documentation