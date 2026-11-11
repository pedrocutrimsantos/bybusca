# Diagnóstico: resultados “hotéis” / irrelevantes na aba Web

## Sintoma

Busca por termos como `Wikipedia` mostra resultados de **Pesquisa de hotéis do Google**
(ex.: Elevador Lacerda, Inhotim) em vez de `wikipedia.org` ou fontes educacionais.

## Causas comuns

1. Engine CSE não está em modo **busca web educacional** (Sites to search).
2. Query antiga do ByBusca forçava `(educação OR escola OR …)`, distorcendo o termo.
3. SafeSearch/exclusões ok, mas **Include sites** vazio ou “entire web” sem foco.

## Correção no painel Google (obrigatória)

1. Abra https://programmablesearchengine.google.com/
2. Selecione o engine `e54b7047fc7e64e10`
3. **Setup → Sites to search**
   - Cole `sites-incluidos-cse.txt` (+ domínio da escola)
   - Ative **Search only included sites** (recomendado)
4. **Sites to exclude** ← `sites-excluidos-cse.txt`
5. **SafeSearch** = ativo
6. Salve e aguarde alguns minutos
7. Teste na URL pública:  
   https://cse.google.com/cse?cx=e54b7047fc7e64e10  
   Busque `Wikipedia` — deve priorizar wikipedia.org / fontes da lista.

Se o engine foi criado como outro produto (ex. hotéis), **crie um novo Programmable Search Engine** do tipo “Web search”, copie o novo `cx` para `.env` (`GOOGLE_PSE_CX` / `VITE_GOOGLE_PSE_CX`).

## Correção no código (já aplicada)

- `buildSafeWebQuery` agora só aplica exclusões (`-porn`, `-apostas`, …), sem OR educacional forçado.
- Widget CSE: `enableImageSearch=false`.
