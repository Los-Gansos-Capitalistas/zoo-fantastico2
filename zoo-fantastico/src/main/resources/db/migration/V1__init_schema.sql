-- ===== ZONE =====
CREATE TABLE zone (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  capacity INT,
  CONSTRAINT uk_zone_name UNIQUE (name)
);

-- ===== CREATURE =====
CREATE TABLE creature (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255),
  species VARCHAR(255),
  size DOUBLE NOT NULL,
  danger_level INT NOT NULL,
  health_status VARCHAR(255),
  zone_id BIGINT NOT NULL,
  CONSTRAINT fk_creature_zone
    FOREIGN KEY (zone_id) REFERENCES zone (id)
      ON UPDATE CASCADE
      ON DELETE RESTRICT
);

-- Índices
CREATE INDEX idx_creature_zone_id ON creature(zone_id);
