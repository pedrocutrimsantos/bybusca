package br.bybusca.vision

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ImageSearchServiceTest {
    @Test
    fun `isUnsafeSafeSearch blocks adult LIKELY`() {
        assertTrue(
            ImageSearchService.isUnsafeSafeSearch(
                mapOf("adult" to "LIKELY", "violence" to "VERY_UNLIKELY")
            )
        )
    }

    @Test
    fun `isUnsafeSafeSearch allows VERY_UNLIKELY adult and violence`() {
        assertFalse(
            ImageSearchService.isUnsafeSafeSearch(
                mapOf("adult" to "VERY_UNLIKELY", "violence" to "VERY_UNLIKELY", "racy" to "UNLIKELY")
            )
        )
    }
}
