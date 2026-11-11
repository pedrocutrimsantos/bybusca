/**
 * Política de busca web segura e educacional.
 * SafeSearch + exclusões (adulto, apostas, streaming pirata) + viés educacional.
 *
 * Lista completa para o painel Google CSE:
 * docs/18.3-regras-fluxos-integracoes/SITES-EXCLUIDOS.md
 */

/** Domínios bloqueados (match parcial no hostname). */
export const BLOCKED_HOST_FRAGMENTS = [
  // adulto / NSFW
  'pornhub',
  'xvideos',
  'xhamster',
  'xnxx',
  'redtube',
  'youporn',
  'tube8',
  'spankbang',
  'onlyfans',
  'fansly',
  'chaturbate',
  'stripchat',
  'bongacams',
  'livejasmin',
  'cam4',
  'myfreecams',
  'pornhd',
  'hqporner',
  'eporner',
  'beeg',
  'nhentai',
  'rule34',
  'gelbooru',
  'hentaihaven',
  'literotica',
  'ashleymadison',
  'adultfriendfinder',
  'fetlife',
  // apostas / azar
  'bet365',
  'betfair',
  'betano',
  'stake.',
  'stake.com',
  'blaze.',
  'blaze.com',
  'sportingbet',
  'betway',
  'pixbet',
  'galera.bet',
  'galerabet',
  'betnacional',
  'esportesdasorte',
  'kto.com',
  'kto.bet',
  'novibet',
  'betsson',
  'rivalo',
  '1xbet',
  'melbet',
  'mostbet',
  'parimatch',
  'pokerstars',
  'superbet',
  'h2bet',
  'sortenabet',
  'vaiabet',
  'seubet',
  'bateubet',
  'fazumbet',
  'lotogreen',
  'cassino',
  'casino',
  // streaming / pirataria
  'thepiratebay',
  'piratebay',
  '1337x',
  'rarbg',
  'yts.mx',
  'yts.ag',
  'yify',
  'torrentgalaxy',
  'limetorrents',
  'nyaa.si',
  'eztv',
  'putlocker',
  'fmovies',
  '123movies',
  'soap2day',
  'lookmovie',
  'gomovies',
  'primewire',
  'megafilmes',
  'redecanais',
  'comandotorrents',
  'torrentdosfilmes',
  'baixedetudo',
  'filmesonlinegratis',
  'seriesonlinehd',
  'vizer.tv',
  'superflix',
  'wareztuga',
  'torrent',
] as const

/** Termos negativos aplicados na query web (operadores Google). */
export const WEB_NEGATIVE_OPERATORS = [
  '-pornografia',
  '-porn',
  '-xxx',
  '-nsfw',
  '-onlyfans',
  '-hentai',
  '-apostas',
  '-cassino',
  '-casino',
  '-betting',
  '-gambling',
  '-roleta',
  '-slots',
  '-bet365',
  '-streaming',
  '-torrent',
  '-pirata',
  '-pirate',
  '-putlocker',
  '-fmovies',
] as const

/** Hosts preferidos / incluídos (educação · ciência · gov). */
export const PREFERRED_HOST_FRAGMENTS = [
  // gov / educação BR
  'mec.gov',
  'inep.gov',
  'gov.br',
  'capes.gov',
  'cnpq.br',
  'fiocruz',
  'embrapa',
  'ibge.gov',
  // universidades
  'usp.br',
  'unicamp',
  'ufrj',
  'ufmg',
  'ufrgs',
  'unb.br',
  'unesp',
  'ufscar',
  'ufpr',
  'ufba',
  'ufpe',
  'ufsc',
  'uff.br',
  'ufc.br',
  'impa.br',
  'ita.br',
  'edu.br',
  '.edu',
  // periódicos / ciência / pesquisa
  'scielo',
  'periodicos.capes',
  'arxiv.org',
  'doaj.org',
  'redalyc.org',
  'plos.org',
  'nature.com',
  'sciencedirect',
  'springer.com',
  'ieee.org',
  'acm.org',
  'pubmed',
  'nih.gov',
  'ncbi.nlm.nih',
  'who.int',
  'unesco',
  'oecd.org',
  'semanticscholar',
  'researchgate',
  // referência / ensino
  'wikipedia.org',
  'wikimedia.org',
  'wiktionary',
  'wikibooks',
  'britannica',
  'khanacademy',
  'brasilescola',
  'mundoeducacao',
  'infoescola',
  'todamateria',
  'cienciahoje',
  'fapesp',
  'nasa.gov',
  'cern.ch',
  'esa.int',
  'archive.org',
  'bn.br',
  'geogebra',
  'desmos.com',
  'scratch.mit',
  'code.org',
  'scholar.google',
  'moodle.org',
  'freecodecamp',
] as const

/**
 * Monta query web segura.
 * Só aplica operadores de exclusão (adulto/apostas/streaming).
 * NÃO força "(educação OR …)" — isso distorce buscas específicas (ex.: Wikipedia)
 * e pode puxar resultados irrelevantes. O foco educacional vem do CSE
 * (Sites to search + SafeSearch) e do pós-filtro de URL.
 */
export function buildSafeWebQuery(raw: string): string {
  const base = raw.trim().replace(/\s+/g, ' ')
  if (!base) return ''
  return `${base} ${WEB_NEGATIVE_OPERATORS.join(' ')}`
}

export function isBlockedWebUrl(url: string): boolean {
  try {
    const host = new URL(url).hostname.toLowerCase()
    return BLOCKED_HOST_FRAGMENTS.some((frag) => host.includes(frag.replace(/^\./, '')))
  } catch {
    return true
  }
}

export function isPreferredEducationalUrl(url: string): boolean {
  try {
    const host = new URL(url).hostname.toLowerCase()
    return PREFERRED_HOST_FRAGMENTS.some((frag) => host.includes(frag.replace(/^\./, '')))
  } catch {
    return false
  }
}
