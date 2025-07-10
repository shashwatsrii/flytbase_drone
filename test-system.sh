#!/bin/bash

echo "=== Drone Survey Management System Test Script ==="
echo

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Base URL
BASE_URL="http://localhost:8080/api"
TOKEN=""

# Test user credentials
TEST_USER="testuser_$(date +%s)"
TEST_PASS="Test123!"
TEST_ORG="TestOrg_$(date +%s)"

echo -e "${YELLOW}1. Testing Backend Health${NC}"
curl -s ${BASE_URL}/health > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Backend is running${NC}"
else
    echo -e "${RED}✗ Backend is not running${NC}"
    exit 1
fi

echo
echo -e "${YELLOW}2. Testing User Registration${NC}"
REGISTER_RESPONSE=$(curl -s -X POST ${BASE_URL}/auth/register \
  -H "Content-Type: application/json" \
  -d "{
    \"fullName\": \"Test User\",
    \"password\": \"${TEST_PASS}\",
    \"email\": \"${TEST_USER}@test.com\",
    \"organizationName\": \"${TEST_ORG}\"
  }")

if [[ $REGISTER_RESPONSE == *"id"* ]]; then
    echo -e "${GREEN}✓ Registration successful${NC}"
    
    # Now login to get token
    LOGIN_RESPONSE=$(curl -s -X POST ${BASE_URL}/auth/login \
      -H "Content-Type: application/json" \
      -d "{
        \"email\": \"${TEST_USER}@test.com\",
        \"password\": \"${TEST_PASS}\"
      }")
    
    if [[ $LOGIN_RESPONSE == *"token"* ]]; then
        echo -e "${GREEN}✓ Login successful${NC}"
        TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    else
        echo -e "${RED}✗ Login failed: $LOGIN_RESPONSE${NC}"
    fi
else
    echo -e "${RED}✗ Registration failed: $REGISTER_RESPONSE${NC}"
fi

echo
echo -e "${YELLOW}3. Testing Database Tables${NC}"
TABLES=$(psql -U shashwatsrii -d drone_survey_db -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';")
echo "Total tables in database: $TABLES"

# Check for new tables from V2 migration
NEW_TABLES=("drone_telemetry" "mission_alerts" "mission_media" "mission_weather" "drone_maintenance" "geofences" "mission_checkpoints" "audit_logs" "mission_progress_cache" "websocket_sessions" "dashboard_statistics")

for table in "${NEW_TABLES[@]}"; do
    EXISTS=$(psql -U shashwatsrii -d drone_survey_db -t -c "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = '$table');")
    if [[ $EXISTS == *"t"* ]]; then
        echo -e "${GREEN}✓ Table $table exists${NC}"
    else
        echo -e "${RED}✗ Table $table missing${NC}"
    fi
done

echo
echo -e "${YELLOW}4. Testing Drone Creation${NC}"
DRONE_RESPONSE=$(curl -s -X POST ${BASE_URL}/drones \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Test Drone 1",
    "model": "DJI Phantom 4",
    "serialNumber": "SN123456",
    "batteryLevel": 100,
    "homeLocationLatitude": 37.7749,
    "homeLocationLongitude": -122.4194
  }')

if [[ $DRONE_RESPONSE == *"id"* ]]; then
    echo -e "${GREEN}✓ Drone created successfully${NC}"
    DRONE_ID=$(echo $DRONE_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
else
    echo -e "${RED}✗ Drone creation failed: $DRONE_RESPONSE${NC}"
fi

echo
echo -e "${YELLOW}5. Testing Survey Area Creation${NC}"
SURVEY_RESPONSE=$(curl -s -X POST ${BASE_URL}/survey-areas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Test Survey Area",
    "description": "Test area for system testing",
    "boundaryPolygon": "{\"type\":\"Polygon\",\"coordinates\":[[[-122.42,37.77],[-122.41,37.77],[-122.41,37.78],[-122.42,37.78],[-122.42,37.77]]]}",
    "areaSize": 100000
  }')

if [[ $SURVEY_RESPONSE == *"id"* ]]; then
    echo -e "${GREEN}✓ Survey area created successfully${NC}"
    SURVEY_ID=$(echo $SURVEY_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
else
    echo -e "${RED}✗ Survey area creation failed: $SURVEY_RESPONSE${NC}"
fi

echo
echo -e "${YELLOW}6. Testing Mission Creation${NC}"
if [ ! -z "$DRONE_ID" ] && [ ! -z "$SURVEY_ID" ]; then
    MISSION_RESPONSE=$(curl -s -X POST ${BASE_URL}/missions \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d "{
        \"name\": \"Test Mission\",
        \"description\": \"Automated test mission\",
        \"droneId\": \"$DRONE_ID\",
        \"surveyAreaId\": \"$SURVEY_ID\",
        \"type\": \"SURVEY\",
        \"scheduledStart\": \"$(date -u +"%Y-%m-%dT%H:%M:%S")\",
        \"flightAltitude\": 100,
        \"speed\": 10.0,
        \"overlapPercentage\": 70,
        \"patternType\": \"LINEAR\"
      }")
    
    if [[ $MISSION_RESPONSE == *"id"* ]]; then
        echo -e "${GREEN}✓ Mission created successfully${NC}"
        MISSION_ID=$(echo $MISSION_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
    else
        echo -e "${RED}✗ Mission creation failed: $MISSION_RESPONSE${NC}"
    fi
fi

echo
echo -e "${YELLOW}7. Testing Mission Start${NC}"
if [ ! -z "$MISSION_ID" ]; then
    START_RESPONSE=$(curl -s -X POST ${BASE_URL}/missions/${MISSION_ID}/start \
      -H "Authorization: Bearer $TOKEN")
    
    if [[ $START_RESPONSE == *"ACTIVE"* ]]; then
        echo -e "${GREEN}✓ Mission started successfully${NC}"
    else
        echo -e "${RED}✗ Mission start failed: $START_RESPONSE${NC}"
    fi
fi

echo
echo -e "${YELLOW}8. Testing Telemetry Endpoint${NC}"
if [ ! -z "$MISSION_ID" ]; then
    TELEMETRY_RESPONSE=$(curl -s -X POST ${BASE_URL}/telemetry/missions/${MISSION_ID} \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d '{
        "latitude": 37.7749,
        "longitude": -122.4194,
        "altitude": 100.0,
        "gpsSatellites": 12,
        "heading": 90.0,
        "groundSpeed": 10.0,
        "batteryLevel": 95
      }')
    
    if [[ $TELEMETRY_RESPONSE == *"id"* ]]; then
        echo -e "${GREEN}✓ Telemetry recorded successfully${NC}"
    else
        echo -e "${RED}✗ Telemetry recording failed: $TELEMETRY_RESPONSE${NC}"
    fi
fi

echo
echo -e "${YELLOW}9. Testing Dashboard Stats${NC}"
STATS_RESPONSE=$(curl -s ${BASE_URL}/dashboard/stats \
  -H "Authorization: Bearer $TOKEN")

if [[ $STATS_RESPONSE == *"totalDrones"* ]]; then
    echo -e "${GREEN}✓ Dashboard stats API working${NC}"
else
    echo -e "${RED}✗ Dashboard stats failed: $STATS_RESPONSE${NC}"
fi

echo
echo -e "${YELLOW}10. Testing WebSocket Connection${NC}"
# This would need a WebSocket client, so we'll just check if the endpoint exists
WS_CHECK=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ws)
if [ $WS_CHECK -eq 200 ] || [ $WS_CHECK -eq 101 ]; then
    echo -e "${GREEN}✓ WebSocket endpoint available${NC}"
else
    echo -e "${RED}✗ WebSocket endpoint not available${NC}"
fi

echo
echo -e "${YELLOW}=== Test Summary ===${NC}"
echo "User: ${TEST_USER}"
echo "Organization: ${TEST_ORG}"
echo "Token: ${TOKEN:0:20}..."
if [ ! -z "$DRONE_ID" ]; then echo "Drone ID: $DRONE_ID"; fi
if [ ! -z "$SURVEY_ID" ]; then echo "Survey Area ID: $SURVEY_ID"; fi
if [ ! -z "$MISSION_ID" ]; then echo "Mission ID: $MISSION_ID"; fi

echo
echo -e "${YELLOW}Frontend URLs to test:${NC}"
echo "1. Login: http://localhost:3000/login"
echo "2. Dashboard: http://localhost:3000/app/dashboard"
echo "3. Fleet Management: http://localhost:3000/app/fleet"
echo "4. Survey Areas: http://localhost:3000/app/survey-areas"
echo "5. Mission Planning: http://localhost:3000/app/mission-planning"
echo "6. Mission Monitoring: http://localhost:3000/app/mission-monitoring"
echo "7. Reports: http://localhost:3000/app/reports"