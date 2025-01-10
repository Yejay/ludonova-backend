-- Add description column to games table
ALTER TABLE games ADD COLUMN IF NOT EXISTS description TEXT; 