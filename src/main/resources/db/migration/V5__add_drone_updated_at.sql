-- Add updated_at column to drones table if it doesn't exist
ALTER TABLE drones ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Update existing records to have updated_at same as created_at
UPDATE drones SET updated_at = created_at WHERE updated_at IS NULL;

-- Make updated_at NOT NULL after populating
ALTER TABLE drones ALTER COLUMN updated_at SET NOT NULL;

-- Also add updated_at to other tables that might be missing it
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE survey_areas ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE missions ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;