-- Initial schema for the Drone Survey Management System

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Organizations table
CREATE TABLE IF NOT EXISTS organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL
);

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Drones table
CREATE TABLE IF NOT EXISTS drones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    name VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    serial_number VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    current_battery_level INTEGER NOT NULL,
    home_location_latitude DOUBLE PRECISION NOT NULL,
    home_location_longitude DOUBLE PRECISION NOT NULL,
    last_maintenance_date DATE,
    total_flight_hours DOUBLE PRECISION NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    created_by BIGINT NOT NULL REFERENCES users(id)
);

-- Survey areas table
CREATE TABLE IF NOT EXISTS survey_areas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    boundary_polygon TEXT NOT NULL,
    area DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by BIGINT NOT NULL REFERENCES users(id)
);

-- Missions table
CREATE TABLE IF NOT EXISTS missions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    drone_id UUID NOT NULL REFERENCES drones(id),
    survey_area_id UUID NOT NULL REFERENCES survey_areas(id),
    pattern_type VARCHAR(50) NOT NULL,
    altitude INTEGER NOT NULL,
    speed DOUBLE PRECISION NOT NULL,
    overlap_percentage INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    scheduled_start TIMESTAMP,
    actual_start TIMESTAMP,
    actual_end TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by BIGINT NOT NULL REFERENCES users(id)
);

-- Flight paths table
CREATE TABLE IF NOT EXISTS flight_paths (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mission_id UUID NOT NULL UNIQUE REFERENCES missions(id),
    waypoints TEXT NOT NULL,
    total_distance DECIMAL(10, 2) NOT NULL,
    estimated_duration INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Add indexes
CREATE INDEX IF NOT EXISTS idx_users_organization_id ON users(organization_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_drones_organization_id ON drones(organization_id);
CREATE INDEX IF NOT EXISTS idx_drones_status ON drones(status);
CREATE INDEX IF NOT EXISTS idx_survey_areas_organization_id ON survey_areas(organization_id);
CREATE INDEX IF NOT EXISTS idx_missions_organization_id ON missions(organization_id);
CREATE INDEX IF NOT EXISTS idx_missions_drone_id ON missions(drone_id);
CREATE INDEX IF NOT EXISTS idx_missions_survey_area_id ON missions(survey_area_id);
CREATE INDEX IF NOT EXISTS idx_missions_status ON missions(status);
CREATE INDEX IF NOT EXISTS idx_flight_paths_mission_id ON flight_paths(mission_id);