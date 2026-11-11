package br.bybusca.search

import br.bybusca.vision.ImageHit
import br.bybusca.vision.ImageSearchService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1")
class ImageSearchController(private val images: ImageSearchService) {
    data class ImageSearchResponse(val items: List<ImageHit>, val warnings: List<String> = emptyList())
    data class ImageUrlRequest(val imageUrl: String)

    @GetMapping("/search/images")
    fun byText(@RequestParam q: String, @RequestParam(defaultValue = "10") size: Int): ImageSearchResponse {
        val (items, warn) = images.byText(q, size)
        return ImageSearchResponse(items, listOfNotNull(warn))
    }

    @PostMapping("/search/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun byFile(@RequestParam("file") file: MultipartFile): ResponseEntity<Any> = try {
        val (items, warn) = images.byUpload(file)
        ResponseEntity.ok(ImageSearchResponse(items, listOfNotNull(warn)))
    } catch (ex: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("code" to "BAD_REQUEST", "message" to (ex.message ?: "Dados invalidos")))
    }

    @PostMapping("/search/image", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun byUrl(@RequestBody body: ImageUrlRequest): ResponseEntity<Any> = try {
        val (items, warn) = images.byUrl(body.imageUrl)
        ResponseEntity.ok(ImageSearchResponse(items, listOfNotNull(warn)))
    } catch (ex: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("code" to "BAD_REQUEST", "message" to (ex.message ?: "Dados invalidos")))
    }
}
