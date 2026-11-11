package br.bybusca.vision

import org.springframework.web.multipart.MultipartFile

class ImageUploadValidator(private val maxBytes: Long) {
    private val allowed = setOf("image/jpeg", "image/png", "image/webp")

    fun requireValid(file: MultipartFile): String {
        if (file.isEmpty) throw IllegalArgumentException("Arquivo de imagem obrigatorio")
        if (file.size > maxBytes) throw IllegalArgumentException("Imagem excede 5 MB")
        val ct = (file.contentType ?: "").lowercase()
        if (ct !in allowed) throw IllegalArgumentException("Tipo invalido. Use JPEG, PNG ou WebP")
        return ct
    }
}
