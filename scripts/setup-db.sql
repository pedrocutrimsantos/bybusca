-- Execute como superusuario Postgres (ajuste -U conforme sua instalação):
--   psql -U postgres -h localhost -f scripts/setup-db.sql

DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'bybusca') THEN
    CREATE ROLE bybusca LOGIN PASSWORD 'bybusca';
  END IF;
END
$$;

SELECT 'CREATE DATABASE bybusca OWNER bybusca'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'bybusca')\gexec

GRANT ALL PRIVILEGES ON DATABASE bybusca TO bybusca;
