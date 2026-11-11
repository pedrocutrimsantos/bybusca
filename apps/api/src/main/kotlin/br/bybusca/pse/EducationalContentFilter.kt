package br.bybusca.pse

import java.net.URI

/**
 * Filtro educacional da busca web: SafeSearch + denylist + preferência acadêmica.
 * Lista completa para o painel CSE: docs/18.3-regras-fluxos-integracoes/SITES-EXCLUIDOS.md
 */
object EducationalContentFilter {
    val blockedHostFragments = listOf(
        // adulto
        "pornhub", "xvideos", "xhamster", "xnxx", "redtube", "youporn", "tube8", "spankbang",
        "onlyfans", "fansly", "chaturbate", "stripchat", "bongacams", "livejasmin", "cam4",
        "myfreecams", "pornhd", "hqporner", "eporner", "beeg", "nhentai", "rule34", "gelbooru",
        "hentaihaven", "literotica", "ashleymadison", "adultfriendfinder", "fetlife",
        // apostas
        "bet365", "betfair", "betano", "stake.", "stake.com", "blaze.", "blaze.com",
        "sportingbet", "betway", "pixbet", "galera.bet", "galerabet", "betnacional",
        "esportesdasorte", "kto.com", "kto.bet", "novibet", "betsson", "rivalo", "1xbet",
        "melbet", "mostbet", "parimatch", "pokerstars", "superbet", "h2bet", "sortenabet",
        "vaiabet", "seubet", "bateubet", "fazumbet", "lotogreen", "cassino", "casino",
        // streaming / pirataria
        "thepiratebay", "piratebay", "1337x", "rarbg", "yts.mx", "yts.ag", "yify",
        "torrentgalaxy", "limetorrents", "nyaa.si", "eztv", "putlocker", "fmovies",
        "123movies", "soap2day", "lookmovie", "gomovies", "primewire", "megafilmes",
        "redecanais", "comandotorrents", "torrentdosfilmes", "baixedetudo",
        "filmesonlinegratis", "seriesonlinehd", "vizer.tv", "superflix", "wareztuga", "torrent"
    )

    private val negativeOperators = listOf(
        "-pornografia", "-porn", "-xxx", "-nsfw", "-onlyfans", "-hentai",
        "-apostas", "-cassino", "-casino", "-betting", "-gambling", "-roleta", "-slots", "-bet365",
        "-streaming", "-torrent", "-pirata", "-pirate", "-putlocker", "-fmovies"
    )

    private val preferredHostFragments = listOf(
        // gov / educação
        "mec.gov", "inep.gov", "gov.br", "capes.gov", "cnpq.br", "fiocruz", "embrapa", "ibge.gov",
        // universidades
        "usp.br", "unicamp", "ufrj", "ufmg", "ufrgs", "unb.br", "unesp", "ufscar", "ufpr",
        "ufba", "ufpe", "ufsc", "uff.br", "ufc.br", "impa.br", "ita.br", "edu.br", ".edu",
        // periódicos / pesquisa
        "scielo", "periodicos.capes", "arxiv.org", "doaj.org", "redalyc.org", "plos.org",
        "nature.com", "sciencedirect", "springer.com", "ieee.org", "acm.org",
        "pubmed", "nih.gov", "ncbi.nlm.nih", "who.int", "unesco", "oecd.org",
        "semanticscholar", "researchgate",
        // referência / ensino
        "wikipedia.org", "wikimedia.org", "wiktionary", "wikibooks", "britannica", "khanacademy",
        "brasilescola", "mundoeducacao", "infoescola", "todamateria", "cienciahoje", "fapesp",
        "nasa.gov", "cern.ch", "esa.int", "archive.org", "bn.br", "geogebra", "desmos.com",
        "scratch.mit", "code.org", "scholar.google", "moodle.org", "freecodecamp"
    )

    fun buildSafeWebQuery(raw: String): String {
        val base = raw.trim().replace(Regex("\\s+"), " ")
        if (base.isBlank()) return ""
        // Apenas exclusões; SafeSearch + Sites to search no CSE definem o escopo educacional.
        return "$base ${negativeOperators.joinToString(" ")}"
    }

    fun isBlocked(url: String): Boolean {
        val host = hostOf(url) ?: return true
        return blockedHostFragments.any { host.contains(it.removePrefix(".")) }
    }

    fun isPreferred(url: String): Boolean {
        val host = hostOf(url) ?: return false
        return preferredHostFragments.any { host.contains(it.removePrefix(".")) }
    }

    fun scoreBoost(url: String): Double = if (isPreferred(url)) 0.25 else 0.0

    private fun hostOf(url: String): String? = try {
        URI(url).host?.lowercase()
    } catch (_: Exception) {
        null
    }
}
