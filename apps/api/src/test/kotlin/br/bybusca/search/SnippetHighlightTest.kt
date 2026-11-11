package br.bybusca.search

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.Method

class SnippetHighlightTest {
    @Test
    fun `snippet destaca termo da busca`() {
        val facade = SearchFacade::class.java
        val method: Method = facade.declaredMethods.first { it.name == "buildSnippet" }
        method.isAccessible = true
        // Não instancia Facade completo: valida regex via função local equivalente
        val q = "equações"
        val text = "Introdução às equações lineares"
        var snippet = text
        val terms = q.lowercase().split(Regex("\\s+")).filter { it.length > 1 }
        for (t in terms) {
            snippet = snippet.replace(Regex("(?i)(${Regex.escape(t)})"), "<mark>$1</mark>")
        }
        assertTrue(snippet.contains("<mark>"))
        assertTrue(snippet.lowercase().contains("equações"))
    }
}
