DROP TABLE IF EXISTS hits;

CREATE TABLE IF NOT EXISTS hits (
  hit_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  app VARCHAR(255) NOT NULL,
  url VARCHAR(512) NOT NULL,
  ip VARCHAR(100) NOT NULL,
  moment TIMESTAMP WITHOUT TIME ZONE,

  CONSTRAINT pk_hit PRIMARY KEY (hit_id)
);