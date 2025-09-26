-- Add overdue column to tasks table
ALTER TABLE tasks ADD COLUMN overdue BOOLEAN DEFAULT FALSE NOT NULL;

-- Add index for better performance when querying overdue tasks
CREATE INDEX idx_tasks_overdue ON tasks(overdue) WHERE overdue = TRUE;
