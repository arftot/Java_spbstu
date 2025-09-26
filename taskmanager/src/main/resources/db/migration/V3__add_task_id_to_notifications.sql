-- Add task_id column to notifications table
ALTER TABLE notifications ADD COLUMN task_id VARCHAR(36);

-- Add index for better performance
CREATE INDEX idx_notifications_task_id ON notifications(task_id);
