-- Add overdue column to tasks table
ALTER TABLE tasks ADD COLUMN overdue BOOLEAN DEFAULT FALSE;

-- Update existing records to set overdue = false
UPDATE tasks SET overdue = FALSE WHERE overdue IS NULL;

-- Make column NOT NULL after updating existing records
ALTER TABLE tasks ALTER COLUMN overdue SET NOT NULL;

-- Add index for better performance when querying overdue tasks
CREATE INDEX IF NOT EXISTS idx_tasks_overdue ON tasks(overdue) WHERE overdue = TRUE;
