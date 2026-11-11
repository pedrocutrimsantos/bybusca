package br.bybusca.vision

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile

class ImageUploadValidatorTest {
    private val v = ImageUploadValidator(maxBytes = 5 * 1024 * 1024)

    @Test
    fun `rejects empty file`() {
        val f = MockMultipartFile("file", "x.jpg", "image/jpeg", ByteArray(0))
        assertThrows(IllegalArgumentException::class.java) { v.requireValid(f) }
    }

    @Test
    fun `rejects non-image content type`() {
        val f = MockMultipartFile("file", "x.pdf", "application/pdf", byteArrayOf(1, 2, 3))
        assertThrows(IllegalArgumentException::class.java) { v.requireValid(f) }
    }

    @Test
    fun `accepts jpeg under limit`() {
        val f = MockMultipartFile("file", "x.jpg", "image/jpeg", ByteArray(100) { 1 })
        assertEquals("image/jpeg", v.requireValid(f))
    }
}
