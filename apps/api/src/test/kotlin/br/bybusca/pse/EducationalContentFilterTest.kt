package br.bybusca.pse

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EducationalContentFilterTest {
    @Test
    fun `bloqueia dominios adultos e apostas`() {
        assertTrue(EducationalContentFilter.isBlocked("https://www.pornhub.com/video"))
        assertTrue(EducationalContentFilter.isBlocked("https://bet365.com/aposta"))
        assertTrue(EducationalContentFilter.isBlocked("https://putlocker.vip/filme"))
    }

    @Test
    fun `permite fontes educacionais`() {
        assertFalse(EducationalContentFilter.isBlocked("https://pt.wikipedia.org/wiki/Equacao"))
        assertFalse(EducationalContentFilter.isBlocked("https://www.scielo.br/artigo"))
        assertTrue(EducationalContentFilter.isPreferred("https://www.usp.br/noticia"))
    }

    @Test
    fun `query segura inclui operadores negativos sem forcar OR educacional`() {
        val q = EducationalContentFilter.buildSafeWebQuery("Wikipedia")
        assertTrue(q.contains("Wikipedia"))
        assertTrue(q.contains("-apostas"))
        assertTrue(q.contains("-porn"))
        assertFalse(q.contains("educação OR"))
    }
}
