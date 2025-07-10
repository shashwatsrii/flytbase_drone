-- Create mission_progress table
CREATE TABLE mission_progress (
    id UUID PRIMARY KEY,
    mission_id UUID NOT NULL,
    current_waypoint_index INTEGER NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    altitude INTEGER NOT NULL,
    speed DOUBLE PRECISION NOT NULL,
    battery_level INTEGER NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_mission_progress_mission FOREIGN KEY (mission_id) REFERENCES missions(id)
);

-- Create index for faster lookups by mission_id
CREATE INDEX idx_mission_progress_mission_id ON mission_progress(mission_id);

-- Create index for timestamp-based queries
CREATE INDEX idx_mission_progress_timestamp ON mission_progress(timestamp);