-- V2: Add production-ready tables for telemetry, alerts, media, and maintenance

-- 1. Drone Telemetry Table for persisting real-time data
CREATE TABLE drone_telemetry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mission_id UUID REFERENCES missions(id) ON DELETE CASCADE,
    drone_id UUID REFERENCES drones(id) ON DELETE CASCADE,
    timestamp TIMESTAMP NOT NULL,
    gps_latitude DOUBLE PRECISION NOT NULL,
    gps_longitude DOUBLE PRECISION NOT NULL,
    gps_altitude DOUBLE PRECISION NOT NULL,
    gps_satellites INTEGER,
    gps_hdop DECIMAL(3,1),
    heading DECIMAL(5,2),
    pitch DECIMAL(5,2),
    roll DECIMAL(5,2),
    ground_speed DECIMAL(10,2),
    vertical_speed DECIMAL(10,2),
    battery_voltage DECIMAL(5,2),
    battery_current DECIMAL(5,2),
    battery_level INTEGER,
    battery_temperature DECIMAL(5,2),
    motor_rpm JSONB,
    signal_strength INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for efficient time-series queries
CREATE INDEX idx_telemetry_mission_timestamp ON drone_telemetry(mission_id, timestamp DESC);
CREATE INDEX idx_telemetry_drone_timestamp ON drone_telemetry(drone_id, timestamp DESC);

-- 2. Mission Alerts Table
CREATE TABLE mission_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mission_id UUID REFERENCES missions(id) ON DELETE CASCADE,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('INFO', 'WARNING', 'CRITICAL')),
    message TEXT NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_by BIGINT REFERENCES users(id),
    acknowledged_at TIMESTAMP,
    resolution_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_alerts_mission ON mission_alerts(mission_id);
CREATE INDEX idx_alerts_unacknowledged ON mission_alerts(acknowledged) WHERE acknowledged = FALSE;

-- 3. Mission Media Table
CREATE TABLE mission_media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mission_id UUID REFERENCES missions(id) ON DELETE CASCADE,
    media_type VARCHAR(20) NOT NULL CHECK (media_type IN ('IMAGE', 'VIDEO', 'THERMAL', 'MULTISPECTRAL')),
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    captured_at TIMESTAMP NOT NULL,
    gps_latitude DOUBLE PRECISION,
    gps_longitude DOUBLE PRECISION,
    gps_altitude DOUBLE PRECISION,
    gimbal_pitch DECIMAL(5,2),
    gimbal_roll DECIMAL(5,2),
    gimbal_yaw DECIMAL(5,2),
    metadata JSONB,
    processed BOOLEAN DEFAULT FALSE,
    thumbnail_path VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_media_mission ON mission_media(mission_id);
CREATE INDEX idx_media_captured_at ON mission_media(captured_at);

-- 4. Mission Weather Data
CREATE TABLE mission_weather (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mission_id UUID REFERENCES missions(id) ON DELETE CASCADE,
    recorded_at TIMESTAMP NOT NULL,
    temperature DECIMAL(5,2),
    humidity INTEGER CHECK (humidity >= 0 AND humidity <= 100),
    wind_speed DECIMAL(5,2),
    wind_direction INTEGER CHECK (wind_direction >= 0 AND wind_direction <= 360),
    visibility DECIMAL(10,2),
    precipitation DECIMAL(5,2),
    pressure DECIMAL(6,2),
    weather_condition VARCHAR(50),
    cloud_coverage INTEGER CHECK (cloud_coverage >= 0 AND cloud_coverage <= 100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_weather_mission ON mission_weather(mission_id);

-- 5. Drone Maintenance Records
CREATE TABLE drone_maintenance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    drone_id UUID REFERENCES drones(id) ON DELETE CASCADE,
    maintenance_type VARCHAR(50) NOT NULL,
    description TEXT,
    performed_by BIGINT REFERENCES users(id),
    performed_at TIMESTAMP NOT NULL,
    next_due_date DATE,
    next_due_hours DECIMAL(10,2),
    parts_replaced JSONB,
    cost DECIMAL(10,2),
    notes TEXT,
    attachments JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_maintenance_drone ON drone_maintenance(drone_id);
CREATE INDEX idx_maintenance_due_date ON drone_maintenance(next_due_date);

-- 6. Geofences and No-Fly Zones
CREATE TABLE geofences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('NO_FLY', 'RESTRICTED', 'WARNING', 'OPERATIONAL')),
    boundary_polygon TEXT NOT NULL,
    min_altitude INTEGER DEFAULT 0,
    max_altitude INTEGER DEFAULT 400,
    active BOOLEAN DEFAULT TRUE,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_geofences_org ON geofences(organization_id);
CREATE INDEX idx_geofences_active ON geofences(active);

-- 7. Mission Checkpoints
CREATE TABLE mission_checkpoints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mission_id UUID REFERENCES missions(id) ON DELETE CASCADE,
    checkpoint_index INTEGER NOT NULL,
    name VARCHAR(255),
    expected_time TIMESTAMP,
    actual_time TIMESTAMP,
    status VARCHAR(50) CHECK (status IN ('PENDING', 'REACHED', 'SKIPPED', 'FAILED')),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_checkpoints_mission ON mission_checkpoints(mission_id);

-- 8. Audit Logs
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(255),
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_performed_at ON audit_logs(performed_at DESC);

-- 9. Real-time Mission Progress Cache (replaces frontend cache)
CREATE TABLE mission_progress_cache (
    mission_id UUID PRIMARY KEY REFERENCES missions(id) ON DELETE CASCADE,
    progress_percentage DECIMAL(5,2) NOT NULL DEFAULT 0,
    current_waypoint_index INTEGER DEFAULT 0,
    last_telemetry_id UUID REFERENCES drone_telemetry(id),
    last_known_position JSONB,
    last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 10. WebSocket Sessions
CREATE TABLE websocket_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT REFERENCES users(id),
    session_id VARCHAR(255) UNIQUE NOT NULL,
    mission_id UUID REFERENCES missions(id),
    connected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_ping TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    disconnected_at TIMESTAMP,
    ip_address VARCHAR(45)
);

CREATE INDEX idx_websocket_active ON websocket_sessions(disconnected_at) WHERE disconnected_at IS NULL;

-- Update mission_progress table to include more real-time data
ALTER TABLE mission_progress 
ADD COLUMN IF NOT EXISTS altitude DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS heading DECIMAL(5,2),
ADD COLUMN IF NOT EXISTS ground_speed DECIMAL(10,2),
ADD COLUMN IF NOT EXISTS distance_remaining DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS estimated_time_remaining INTEGER;

-- Add indexes to existing tables that are missing them
CREATE INDEX IF NOT EXISTS idx_missions_status ON missions(status);
CREATE INDEX IF NOT EXISTS idx_missions_scheduled_start ON missions(scheduled_start);
CREATE INDEX IF NOT EXISTS idx_drones_status ON drones(status);
CREATE INDEX IF NOT EXISTS idx_drones_organization ON drones(organization_id);

-- Create a materialized view for dashboard statistics
CREATE MATERIALIZED VIEW dashboard_statistics AS
SELECT 
    o.id as organization_id,
    COUNT(DISTINCT d.id) as total_drones,
    COUNT(DISTINCT m.id) FILTER (WHERE m.status = 'ACTIVE') as active_missions,
    COUNT(DISTINCT m.id) FILTER (WHERE m.status = 'COMPLETED') as completed_missions,
    COUNT(DISTINCT m.id) FILTER (WHERE m.status = 'ABORTED') as aborted_missions,
    COALESCE(SUM(d.total_flight_hours), 0) as total_flight_hours,
    COUNT(DISTINCT sa.id) as total_survey_areas,
    COALESCE(SUM(sa.area), 0) as total_area_coverage
FROM organizations o
LEFT JOIN drones d ON d.organization_id = o.id
LEFT JOIN missions m ON m.organization_id = o.id
LEFT JOIN survey_areas sa ON sa.organization_id = o.id
GROUP BY o.id;

-- Create index on materialized view
CREATE UNIQUE INDEX idx_dashboard_stats_org ON dashboard_statistics(organization_id);

-- Function to refresh dashboard statistics
CREATE OR REPLACE FUNCTION refresh_dashboard_statistics()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY dashboard_statistics;
END;
$$ LANGUAGE plpgsql;

-- Trigger to update drone flight hours when mission completes
CREATE OR REPLACE FUNCTION update_drone_flight_hours()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN
        UPDATE drones 
        SET total_flight_hours = total_flight_hours + 
            EXTRACT(EPOCH FROM (NEW.actual_end - NEW.actual_start)) / 3600
        WHERE id = NEW.drone_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER mission_completion_trigger
AFTER UPDATE ON missions
FOR EACH ROW
EXECUTE FUNCTION update_drone_flight_hours();

-- Function to log telemetry and update progress
CREATE OR REPLACE FUNCTION log_telemetry_and_update_progress()
RETURNS TRIGGER AS $$
BEGIN
    -- Update mission progress cache
    UPDATE mission_progress_cache
    SET 
        last_telemetry_id = NEW.id,
        last_known_position = jsonb_build_object(
            'lat', NEW.gps_latitude,
            'lng', NEW.gps_longitude,
            'alt', NEW.gps_altitude
        ),
        last_update = NEW.timestamp
    WHERE mission_id = NEW.mission_id;
    
    -- Update mission_progress table
    UPDATE mission_progress
    SET 
        current_latitude = NEW.gps_latitude,
        current_longitude = NEW.gps_longitude,
        altitude = NEW.gps_altitude,
        heading = NEW.heading,
        ground_speed = NEW.ground_speed,
        battery_level = NEW.battery_level,
        last_updated = NEW.timestamp
    WHERE mission_id = NEW.mission_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER telemetry_update_trigger
AFTER INSERT ON drone_telemetry
FOR EACH ROW
EXECUTE FUNCTION log_telemetry_and_update_progress();