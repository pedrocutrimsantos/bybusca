# 18.9 — Manual de uso, operação e suporte

| Campo | Valor |
|-------|-------|
| **Produto** | ByBusca |
| **Tarefa** | 18.9 Manual de uso, operação e suporte |
| **Status** | Pronto para envio (v1.1) |
| **Supervisor** | Luis Rodrigo Lima Rodrigues |
| **Participante** | Pedro Cutrim |

---

## 1. Uso — visitante / aluno na rede

1. Abra o ByBusca no navegador da escola  
2. Digite o tema (ex.: “equações 8º ano”)  
3. Pressione Enter  
4. Use as abas: **Tudo · Acervo · Aulas · Arquivos · Páginas · Módulos · Web · Imagens**  
5. Clique no título para abrir o recurso  

**Sem login:** catálogo aberto da rede (`public`/`network`) **e aba Imagens** (texto + foto).  
**Com login:** também materiais da sua turma/disciplina.  
**Aba Web:** busca na internet com filtro educacional (sem adulto/apostas/streaming impróprio).  
**Aba Imagens:** (1) digite um tema e pressione Enter — grade de imagens dos sites educacionais allowlist; (2) **envie uma foto** (JPG/PNG/WebP, até 5 MB) ou **cole a URL** de uma imagem para achar páginas similares na web.

Demo: `aluno` / `senha123`

---

## 2. Uso — professor

1. Entre com usuário institucional (`prof` / `senha123` no demo)  
2. Busque; planos e materiais da disciplina aparecem conforme ACL  
3. Peça indexação de novos materiais à coordenação (ou API de indexação)

---

## 3. Uso — administração

1. Login `admin` / `senha123` → `/admin`  
2. Confira documentos indexados  
3. Clique **Reindexar tenant** após cargas em lote  
4. Confira CX/URL do Google PSE no painel  
5. Secrets (`GOOGLE_PSE_API_KEY`, `GOOGLE_VISION_API_KEY`) só no servidor — nunca no browser  
6. Busca por imagem: `GOOGLE_PSE_ENABLED=true` (texto→imagens); `GOOGLE_VISION_ENABLED=true` + chave Vision (foto→similares) — ver `.env.example`  

Ajuda rápida: `/ajuda`

---

## 4. Operação diária / periódica

| Tarefa | Frequência | Responsável |
|--------|------------|-------------|
| Health API (`/api/v1/health`) e web | Diário | TI |
| Backup Postgres | Diário | TI |
| Revisar auditoria de buscas | Semanal | Coord/TI |
| Revisar sites excluídos CSE | Mensal | TI/Coord |
| Quota PSE (se JSON API) | Mensal | TI |
| Quota Vision (busca por foto) | Mensal | TI |
| Conferir flags `GOOGLE_VISION_*` / upload 5 MB | Após deploy | TI |

Lista de exclusão: `docs/18.3-regras-fluxos-integracoes/SITES-EXCLUIDOS.md`  
Política imagens: `docs/18.3-regras-fluxos-integracoes/POLITICA-BUSCA-WEB.md` (seção Busca por imagem)

---

## 5. Suporte — problemas comuns

| Sintoma | Causa | Ação |
|---------|-------|------|
| Nenhum resultado escola | Índice vazio / tenant | Seed + `X-Tenant` |
| Não vejo material da turma | Sem login / ACL | Entrar como aluno/prof |
| Web vazia ou estranha | CSE/SafeSearch/exclusões | Painel Google + `sites-excluidos-cse.txt` |
| Resultado impróprio na Web | Domínio novo | Incluir em Sites to exclude + código |
| Tela branca | API fora / CORS | Subir API; `VITE_API_BASE_URL` |
| Admin “Sem acesso” | Token sem ADMIN | Login admin |
| Busca sem acento falha | FTS antigo | `POST /index/reindex` (migração V2+) |
| Imagens por texto vazias | PSE off / sem key | `GOOGLE_PSE_ENABLED` + `GOOGLE_PSE_API_KEY` |
| “Busca por foto indisponível” | Vision off / sem key | `GOOGLE_VISION_ENABLED` + `GOOGLE_VISION_API_KEY` |
| Upload rejeitado | > 5 MB ou tipo inválido | JPEG/PNG/WebP até 5 MB |
| “Esta imagem não pode ser usada” | SafeSearch Vision | Outra imagem; conteúdo bloqueado |

---

## 6. Contatos

- TI local: __________________  
- Coordenação: __________________  
- Supervisor do projeto: Luis Rodrigo Lima Rodrigues  

Engine web público: https://cse.google.com/cse?cx=e54b7047fc7e64e10

---

## 7. Critério de conclusão

Manual utilizável por aluno, professor, admin e TI → **18.9 concluída / pronta para envio**.
