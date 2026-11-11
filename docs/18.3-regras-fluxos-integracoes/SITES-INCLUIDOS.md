# Sites a incluir — ByBusca (busca web educacional)

**Uso:** colar no Google Programmable Search → *Setup* → *Sites to search* (incluir)  
**Engine:** `e54b7047fc7e64e10` · https://cse.google.com/cse?cx=e54b7047fc7e64e10  

Formato sugerido no painel CSE: um padrão por linha (`*.dominio` cobre subdomínios).

> **Modo recomendado:** “Search only included sites” (buscar **somente** estes sites)  
> assim a web do ByBusca fica focada em educação/ciência.  
> Se a escola quiser web aberta + preferência educacional, use “Search the entire web” + boost;  
> neste caso a lista ainda ajuda como prioridade no painel.

Complemento: exclusões em [`SITES-EXCLUIDOS.md`](./SITES-EXCLUIDOS.md).

---

## 1. Governo e educação (Brasil)

```text
*.mec.gov.br
*.inep.gov.br
*.gov.br
*.educacao.gov.br
*.fnde.gov.br
*.capes.gov.br
*.cnpq.br
*.brasil.gov.br
*.planalto.gov.br
*.senado.leg.br
*.camara.leg.br
*.bcb.gov.br
*.ibge.gov.br
*.fiocruz.br
*.embrapa.br
*.inpe.br
*.inmetro.gov.br
*.anvisa.gov.br
*.ms.gov.br
*.mma.gov.br
*.mcti.gov.br
```

---

## 2. Universidades e institutos federais (amostra nacional)

```text
*.usp.br
*.unicamp.br
*.ufrj.br
*.ufmg.br
*.ufrgs.br
*.ufscar.br
*.ufpr.br
*.ufba.br
*.ufpe.br
*.ufc.br
*.ufsc.br
*.unb.br
*.uff.br
*.unesp.br
*.ufpa.br
*.ufam.br
*.ufmt.br
*.ufg.br
*.ufs.br
*.ufpi.br
*.ufrn.br
*.ufal.br
*.ufma.br
*.uftm.edu.br
*.ifsp.edu.br
*.ifsc.edu.br
*.ifsuldeminas.edu.br
*.ifrn.edu.br
*.cefetmg.br
*.cefet-rj.br
*.ita.br
*.ime.usp.br
*.impa.br
*.on.br
```

> Inclua também o domínio da **própria escola/rede** e secretarias estaduais/municipais de educação.

---

## 3. Portais acadêmicos e periódicos

```text
*.scielo.br
*.scielo.org
*.periodicos.capes.gov.br
*.lattes.cnpq.br
*.orcid.org
*.doaj.org
*.redalyc.org
*.dialnet.unirioja.es
*.researchgate.net
*.academia.edu
*.ssrn.com
*.arxiv.org
*.biorxiv.org
*.plos.org
*.nature.com
*.science.org
*.sciencedirect.com
*.springer.com
*.wiley.com
*.tandfonline.com
*.ieee.org
*.acm.org
*.pubmed.ncbi.nlm.nih.gov
*.ncbi.nlm.nih.gov
*.nih.gov
*.who.int
*.unesco.org
*.oecd.org
*.worldbank.org
```

---

## 4. Enciclopédias e referência

```text
*.wikipedia.org
*.wikimedia.org
*.wiktionary.org
*.wikibooks.org
*.wikiversity.org
*.wikidata.org
*.britannica.com
*.khanacademy.org
*.pt.khanacademy.org
*.curso-em-video.com
*.brasilescola.uol.com.br
*.mundoeducacao.uol.com.br
*.infoescola.com
*.todamateria.com.br
*.educamaisbrasil.com.br
*.sofisica.com.br
*.manualdomundo.com.br
*.cienciahoje.org.br
*.revistapesquisa.fapesp.br
```

---

## 5. Museus, cultura e ciência aberta

```text
*.museu.ufmg.br
*.museunacional.ufrj.br
*.pinacoteca.org.br
*.itaucultural.org.br
*.bndigital.bn.br
*.bn.br
*.archive.org
*.europeana.eu
*.smithsonianmag.com
*.nasa.gov
*.esa.int
*.cern.ch
*.space.com
*.nationalgeographic.com
*.dw.com
*.bbc.com
*.bbc.co.uk
*.agencia.fiocruz.br
```

> BBC/DW/NatGeo: úteis para atualidades científicas; mantenha SafeSearch ativo.

---

## 6. Ferramentas e plataformas escolares

```text
*.classroom.google.com
*.edu.google.com
*.edu.microsoft.com
*.office.com
*.canva.com
*.padlet.com
*.moodle.org
*.geogebra.org
*.desmos.com
*.scratch.mit.edu
*.code.org
*.freecodecamp.org
*.duolingo.com
*.ankiweb.net
*.quizlet.com
*.scholar.google.com
*.books.google.com
*.translate.google.com
*.maps.google.com
```

> Google Classroom / Scholar: conteúdo útil; o SafeSearch e exclusões continuam ativos.

---

## 7. Lista compacta válida para o CSE (50 domínios)

> **Formato Google (oficial):**  
> - Domínio inteiro: `*.example.com`  
> - Site inteiro: `www.mysite.com/*`  
> **Inválido:** `*.gov.br`, `*.com`, `*.com.br` (sufixo público)  
> **Limite:** no máximo **50 domínios distintos no engine inteiro** (includes **+** excludes juntos)

Arquivo pronto: [`sites-incluidos-cse.txt`](./sites-incluidos-cse.txt) — **50** entradas (cota cheia).

**Pesquisa acadêmica incluída:** SciELO (`.br` + `.org`), arXiv, DOAJ, Redalyc, PLOS, Google Scholar, Nature, NIH/PubMed (via `*.nih.gov`), CAPES/CNPq/FAPESP.

```text
*.mec.gov.br
*.inep.gov.br
*.capes.gov.br
*.cnpq.br
*.fiocruz.br
*.embrapa.br
*.ibge.gov.br
*.usp.br
*.unicamp.br
*.ufrj.br
*.ufmg.br
*.ufrgs.br
*.unb.br
*.unesp.br
*.ufscar.br
*.ufpr.br
*.ufba.br
*.ufpe.br
*.ufsc.br
*.impa.br
*.ita.br
*.scielo.br
*.scielo.org
*.arxiv.org
*.doaj.org
*.redalyc.org
*.plos.org
scholar.google.com/*
*.wikipedia.org
*.britannica.com
*.khanacademy.org
brasilescola.uol.com.br/*
mundoeducacao.uol.com.br/*
*.infoescola.com
*.todamateria.com.br
*.cienciahoje.org.br
*.fapesp.br
*.nasa.gov
*.bn.br
*.geogebra.org
*.desmos.com
*.mit.edu
*.code.org
*.moodle.org
*.freecodecamp.org
*.nih.gov
*.who.int
*.unesco.org
*.archive.org
*.nature.com
```

Para o site da escola: **substitua** 1 entrada menos prioritária (ex.: `*.freecodecamp.org`) por:

```text
*.suasescola.edu.br
```

> Já cobertos sem linha extra: `periodicos.capes.gov.br` (`*.capes.gov.br`), `lattes.cnpq.br` (`*.cnpq.br`), PubMed (`*.nih.gov`).

---

## 8. Como cadastrar no Google CSE

1. Acesse https://programmablesearchengine.google.com/  
2. Selecione o engine `e54b7047fc7e64e10`  
3. **Apague todos os sites já cadastrados** (Include **e** Exclude) — o limite de 50 é do engine inteiro  
4. **Setup** → **Sites to search** → cole [`sites-incluidos-cse.txt`](./sites-incluidos-cse.txt)  
5. Ative **Search only included sites**  
6. **Deixe Sites to exclude vazio** (com include-only, porn/apostas já ficam de fora; exclusões consomem a cota de 50)  
7. **SafeSearch** ativo · Salvar  

### Se aparecer “mais de 50 domínios distintos”

| Causa | O que fazer |
|-------|-------------|
| Já havia sites no engine | Apagar tudo e colar de novo |
| Include + Exclude juntos > 50 | Esvaziar Exclude; usar só Include |
| Lista com exatamente 50 + escola | Remover 1–2 entradas antes de adicionar a escola |
| Formato `*.dominio/*` | Usar `*.dominio` (domínio inteiro) |

### Se aparecer “padrões de URL inválidos”

| Errado | Correto |
|--------|---------|
| `*.gov.br` | `*.mec.gov.br`, `*.inep.gov.br`, … |
| `*.com` / `*.com.br` | Inválido (sufixo público) |
| `*.uol.com.br` | Preferir `brasilescola.uol.com.br/*` (site específico) |

---

## 9. Manutenção

| Frequência | Ação |
|------------|------|
| Por contrato/rede | Incluir domínio oficial da escola e secretaria |
| Semestral | Revisar portais estaduais e novos repositórios |
| Sob demanda | Incluir sites indicados por coordenação pedagógica |

Espelho no código (boost): `PREFERRED_HOST_FRAGMENTS` em `apps/web/src/config/safeSearch.ts` e `EducationalContentFilter.kt`.
