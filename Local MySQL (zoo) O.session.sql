
ALTER TABLE zone
  ADD COLUMN IF NOT EXISTS description VARCHAR(255) NULL,
  ADD COLUMN IF NOT EXISTS capacity INT NULL;


--Evitar duplicados por nombre
ALTER TABLE zone
  ADD CONSTRAINT uk_zone_name UNIQUE (name);

SHOW CREATE TABLE zone;
