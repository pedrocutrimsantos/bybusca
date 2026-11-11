# 18.8 — Instalação, ambiente, deploy e recuperação

| Campo | Valor |
|-------|-------|
| **Produto** | ByBusca |
| **Tarefa** | 18.8 Instalação, ambiente, deploy e recuperação |
| **Status** | Pronto para envio (v1.1) |
| **Supervisor** | Luis Rodrigo Lima Rodrigues |
| **Participante** | Pedro Cutrim |

---

## 1. Requisitos

| Software | Versão |
|----------|--------|
| JDK | 17+ (21 recomendado) |
| Node.js | 20+ |
| PostgreSQL | 16+ (validado em 18.x) |
| Docker Compose | opcional (alternativa ao Postgres local) |
| Git | 2.x |

---

## 2. Ambiente local

### 2.A Com Docker

```powershell
cd C:\Users\User\Repository\ByBusca
Copy-Item .env.example .env
docker compose up -d
cd apps\api ; .\gradlew.bat bootRun
cd apps\web ; npm install ; npm run dev
```

### 2.B Postgres Windows (sem Docker)

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -h localhost -f scripts\setup-db.sql
# Ajuste DB_USER/DB_PASSWORD no .env (ex.: postgres / sua senha)
cd apps\api ; .\gradlew.bat bootRun
cd apps\web ; npm run dev
```

| Serviço | URL |
|---------|-----|
| API | http://localhost:8080 |
| Health | http://localhost:8080/api/v1/health |
| Web | http://127.0.0.1:5173 |

---

## 3. Variáveis essenciais

| Variável | Exemplo |
|----------|---------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/bybusca` |
| `JWT_SECRET` | string ≥ 32 chars |
| `BYBUSCA_DEFAULT_TENANT` | `demo-escola` |
| `GOOGLE_PSE_CX` | `e54b7047fc7e64e10` |
| `VITE_GOOGLE_PSE_CX` | mesmo CX |
| `GOOGLE_PSE_ENABLED` | `false` (widget CSE funciona sem API key) |
| `GOOGLE_PSE_API_KEY` | só se usar JSON API |

Nunca commitar `.env` real.

---

## 4. Deploy na rede da escola

1. `.\gradlew.bat bootJar` → jar da API  
2. `npm run build` → servir `apps/web/dist`  
3. Postgres com backup  
4. Nginx/proxy: `busca.escola.local`  
5. Registrar ByBusca como mecanismo de busca da rede  

```nginx
server {
  server_name busca.escola.local;
  root /var/www/bybusca;
  location /api/ { proxy_pass http://127.0.0.1:8080/api/; }
  location / { try_files $uri /index.html; }
}
```

---

## 4.1 Hostinger VPS (produção)

Requisito: **VPS Ubuntu** (KVM 2 recomendado: 2 vCPU / 8 GB). Hospedagem compartilhada não roda a API Java.

Subdomínio de produção: **http://bybusca.alenxandriaglobaltec.com/** (raiz do buscador; `/usuarios` é de outro sistema).

No hPanel do domínio `alenxandriaglobaltec.com`:

1. DNS → registro **A**  
   - Nome/host: `bybusca`  
   - Aponta para o **IP da VPS**  
   - TTL: automático / 300  
2. Firewall da VPS: **22**, **80**, **443**.

No servidor (SSH como root):

```bash
apt-get update -y
apt-get install -y ca-certificates curl git
curl -fsSL https://get.docker.com | sh
git clone https://github.com/pedrocutrimsantos/bybusca.git /opt/bybusca
cd /opt/bybusca
cp .env.example .env
nano .env
```

No `.env` de produção, obrigatório:

```text
DB_USER=bybusca
DB_PASSWORD=<senha-forte>
JWT_SECRET=<string-aleatoria-32-chars-min>
BYBUSCA_DEFAULT_TENANT=demo-escola
BYBUSCA_CORS_ORIGINS=http://bybusca.alenxandriaglobaltec.com,https://bybusca.alenxandriaglobaltec.com
GOOGLE_PSE_CX=e54b7047fc7e64e10
VITE_GOOGLE_PSE_CX=e54b7047fc7e64e10
VITE_TENANT=demo-escola
```

Subir:

```bash
docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml ps
curl http://127.0.0.1/api/v1/health
```

O Nginx do container `web` serve o React e encaminha `/api/` para a API. HTTPS: instale Certbot no host ou use o SSL do hPanel apontando para a porta 80.

Atualizar depois de um push no GitHub:

```bash
cd /opt/bybusca
git pull
docker compose -f docker-compose.prod.yml up -d --build
```

---

## 5. Google CSE (obrigatório para web filtrada)

1. Painel: https://programmablesearchengine.google.com/  
2. Engine `e54b7047fc7e64e10`  
3. **SafeSearch** ativo  
4. **Sites to exclude** ← `docs/18.3-regras-fluxos-integracoes/sites-excluidos-cse.txt`  
5. URL pública: https://cse.google.com/cse?cx=e54b7047fc7e64e10  

---

## 6. Backup e recuperação

| Ativo | Backup | Restore |
|-------|--------|---------|
| Postgres | `pg_dump` diário | `pg_restore` |
| Secrets | cofre da escola | recriar env |
| Artefatos | jar/dist versionados | redeploy |

**RPO MVP:** 24h · **RTO MVP:** 4h

---

## 7. Incidentes

| Caso | Ação |
|------|------|
| PSE/widget web fora | Busca interna segue; avisar TI |
| API down | Verificar Postgres + logs `bootRun` |
| Flyway checksum | Não editar Vn aplicada; nova migração |
| Conteúdo impróprio na web | Atualizar `SITES-EXCLUIDOS.md` + painel CSE |

---

## 8. Critério de conclusão

Install, deploy, CSE, backup e incidentes documentados → **18.8 concluída / pronta para envio**.
