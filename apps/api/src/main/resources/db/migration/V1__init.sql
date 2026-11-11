-- ByBusca V1 schema + seed demo
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    class_ids TEXT[] NOT NULL DEFAULT '{}',
    subject_ids TEXT[] NOT NULL DEFAULT '{}',
    UNIQUE (tenant_id, username)
);

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    external_id VARCHAR(120) NOT NULL,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(500) NOT NULL,
    body TEXT NOT NULL DEFAULT '',
    url VARCHAR(1000) NOT NULL DEFAULT '',
    visibility VARCHAR(20) NOT NULL DEFAULT 'network',
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    search_vector TSVECTOR,
    indexed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    UNIQUE (tenant_id, external_id)
);

CREATE TABLE document_acl (
    document_id UUID PRIMARY KEY REFERENCES documents(id) ON DELETE CASCADE,
    allowed_roles TEXT[] NOT NULL DEFAULT '{}',
    allowed_class_ids TEXT[] NOT NULL DEFAULT '{}',
    allowed_subject_ids TEXT[] NOT NULL DEFAULT '{}',
    owner_user_id UUID NULL REFERENCES users(id)
);

CREATE TABLE search_audit (
    id BIGSERIAL PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    user_id UUID NULL REFERENCES users(id),
    query VARCHAR(500) NOT NULL,
    ip VARCHAR(64),
    took_ms INT NOT NULL DEFAULT 0,
    sources TEXT[] NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_search_vector ON documents USING GIN (search_vector);
CREATE INDEX idx_documents_tenant_type ON documents (tenant_id, type) WHERE deleted_at IS NULL;

-- Roles
INSERT INTO roles (code, name) VALUES
 ('STUDENT', 'Aluno'),
 ('TEACHER', 'Professor'),
 ('COORDINATOR', 'Coordenação'),
 ('ADMIN', 'Administrador');

-- Tenant demo
INSERT INTO tenants (id, code, name)
VALUES ('11111111-1111-1111-1111-111111111111', 'demo-escola', 'Escola Demo ByBusca');

-- Senha de todos: "senha123" (BCrypt)
-- hash gerado para BCrypt strength 10
INSERT INTO users (id, tenant_id, username, password_hash, display_name, class_ids, subject_ids) VALUES
 ('22222222-2222-2222-2222-222222222201', '11111111-1111-1111-1111-111111111111', 'admin',
  '$2b$10$Ho9L//bojcl9AHxt/a7Pr.b8k078bKFS5SVtf1Vn3DB28NpBEOBtC', 'Admin Escola', '{}', '{}'),
 ('22222222-2222-2222-2222-222222222202', '11111111-1111-1111-1111-111111111111', 'prof',
  '$2b$10$Ho9L//bojcl9AHxt/a7Pr.b8k078bKFS5SVtf1Vn3DB28NpBEOBtC', 'Prof. Ana', '{}', ARRAY['MAT']),
 ('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111111', 'aluno',
  '$2b$10$Ho9L//bojcl9AHxt/a7Pr.b8k078bKFS5SVtf1Vn3DB28NpBEOBtC', 'Aluno João', ARRAY['8A'], '{}');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE u.username = 'admin' AND r.code = 'ADMIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE u.username = 'prof' AND r.code = 'TEACHER';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE u.username = 'aluno' AND r.code = 'STUDENT';

-- Documentos seed
INSERT INTO documents (id, tenant_id, external_id, type, title, body, url, visibility, metadata, search_vector) VALUES
 ('33333333-3333-3333-3333-333333333301', '11111111-1111-1111-1111-111111111111', 'aula-mat-01', 'LESSON',
  'Equações do 1º grau', 'Introdução às equações lineares para o 8º ano.', 'https://escola.local/aulas/mat-01', 'network',
  '{"subject":"Matemática","grade":"8"}'::jsonb,
  setweight(to_tsvector('portuguese', 'Equações do 1º grau'), 'A') ||
  setweight(to_tsvector('portuguese', 'Introdução às equações lineares para o 8º ano.'), 'B')),
 ('33333333-3333-3333-3333-333333333302', '11111111-1111-1111-1111-111111111111', 'acervo-01', 'COLLECTION',
  'Acervo de ciências', 'Coleção de experimentos e textos de ciências naturais.', 'https://escola.local/acervo/ciencias', 'public',
  '{"area":"Ciências"}'::jsonb,
  setweight(to_tsvector('portuguese', 'Acervo de ciências'), 'A') ||
  setweight(to_tsvector('portuguese', 'Coleção de experimentos e textos de ciências naturais.'), 'B')),
 ('33333333-3333-3333-3333-333333333303', '11111111-1111-1111-1111-111111111111', 'arquivo-plano', 'FILE',
  'Plano de aula - equações', 'Arquivo PDF interno do plano de aula de equações.', 'https://escola.local/files/plano-eq.pdf', 'restricted',
  '{"subject":"Matemática"}'::jsonb,
  setweight(to_tsvector('portuguese', 'Plano de aula - equações'), 'A') ||
  setweight(to_tsvector('portuguese', 'Arquivo PDF interno do plano de aula de equações.'), 'B')),
 ('33333333-3333-3333-3333-333333333304', '11111111-1111-1111-1111-111111111111', 'pagina-biblioteca', 'PAGE',
  'Biblioteca escolar', 'Horários e regras da biblioteca da escola.', 'https://escola.local/biblioteca', 'network',
  '{}'::jsonb,
  setweight(to_tsvector('portuguese', 'Biblioteca escolar'), 'A') ||
  setweight(to_tsvector('portuguese', 'Horários e regras da biblioteca da escola.'), 'B')),
 ('33333333-3333-3333-3333-333333333305', '11111111-1111-1111-1111-111111111111', 'modulo-hist', 'MODULE',
  'Módulo História do Brasil', 'Unidade didática sobre o período colonial.', 'https://escola.local/modulos/hist-01', 'network',
  '{"subject":"História"}'::jsonb,
  setweight(to_tsvector('portuguese', 'Módulo História do Brasil'), 'A') ||
  setweight(to_tsvector('portuguese', 'Unidade didática sobre o período colonial.'), 'B')),
 ('33333333-3333-3333-3333-333333333306', '11111111-1111-1111-1111-111111111111', 'aula-8a-privada', 'LESSON',
  'Lista de exercícios turma 8A', 'Lista exclusiva da turma 8A sobre equações.', 'https://escola.local/aulas/8a-lista', 'restricted',
  '{"subject":"Matemática","class":"8A"}'::jsonb,
  setweight(to_tsvector('portuguese', 'Lista de exercícios turma 8A'), 'A') ||
  setweight(to_tsvector('portuguese', 'Lista exclusiva da turma 8A sobre equações.'), 'B'));

INSERT INTO document_acl (document_id, allowed_roles, allowed_class_ids, allowed_subject_ids) VALUES
 ('33333333-3333-3333-3333-333333333301', '{}', '{}', '{}'),
 ('33333333-3333-3333-3333-333333333302', '{}', '{}', '{}'),
 ('33333333-3333-3333-3333-333333333303', ARRAY['TEACHER','ADMIN','COORDINATOR'], '{}', ARRAY['MAT']),
 ('33333333-3333-3333-3333-333333333304', '{}', '{}', '{}'),
 ('33333333-3333-3333-3333-333333333305', '{}', '{}', '{}'),
 ('33333333-3333-3333-3333-333333333306', ARRAY['STUDENT','TEACHER','ADMIN'], ARRAY['8A'], ARRAY['MAT']);
