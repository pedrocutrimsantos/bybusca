-- Melhora busca tipo Google: acentos não impedem o match (equacoes ≈ equações)
CREATE EXTENSION IF NOT EXISTS unaccent;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_ts_config WHERE cfgname = 'portuguese_unaccent'
  ) THEN
    CREATE TEXT SEARCH CONFIGURATION portuguese_unaccent (COPY = portuguese);
    ALTER TEXT SEARCH CONFIGURATION portuguese_unaccent
      ALTER MAPPING FOR hword, hword_part, word
      WITH unaccent, portuguese_stem;
  END IF;
END
$$;

UPDATE documents
SET search_vector =
  setweight(to_tsvector('portuguese_unaccent', coalesce(title, '')), 'A') ||
  setweight(to_tsvector('portuguese_unaccent', coalesce(body, '')), 'B');
