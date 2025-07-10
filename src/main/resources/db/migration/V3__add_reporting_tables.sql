-- Create survey_reports table
CREATE TABLE survey_reports (
    id UUID PRIMARY KEY,
    mission_id UUID NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    total_area DOUBLE PRECISION NOT NULL,
    covered_area DOUBLE PRECISION NOT NULL,
    image_count INTEGER NOT NULL,
    anomalies_detected INTEGER NOT NULL,
    report_data TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_survey_report_mission FOREIGN KEY (mission_id) REFERENCES missions(id)
);

-- Create mission_statistics table
CREATE TABLE mission_statistics (
    id UUID PRIMARY KEY,
    mission_id UUID NOT NULL,
    total_distance DECIMAL(15,2),
    actual_duration INTEGER,
    average_speed DECIMAL(10,2),
    max_altitude INTEGER,
    battery_usage DECIMAL(5,2),
    waypoints_completed INTEGER,
    total_waypoints INTEGER,
    completion_percentage DECIMAL(5,2),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_mission_statistics_mission FOREIGN KEY (mission_id) REFERENCES missions(id),
    CONSTRAINT uk_mission_statistics_mission UNIQUE (mission_id)
);

-- Create indexes for faster lookups
CREATE INDEX idx_survey_report_mission_id ON survey_reports(mission_id);
CREATE INDEX idx_survey_report_generated_at ON survey_reports(generated_at);
CREATE INDEX idx_survey_report_report_type ON survey_reports(report_type);