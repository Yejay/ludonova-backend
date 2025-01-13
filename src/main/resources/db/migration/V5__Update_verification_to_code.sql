ALTER TABLE users
DROP COLUMN verification_token,
DROP COLUMN verification_token_expiry,
ADD COLUMN verification_code VARCHAR(6),
ADD COLUMN verification_code_expiry TIMESTAMP; 