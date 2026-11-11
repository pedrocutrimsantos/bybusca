package br.bybusca.vision

import br.bybusca.config.ByBuscaProperties
import br.bybusca.pse.PseSearchService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.Base64

@Service
class ImageSearchService(
    private val props: ByBuscaProperties,
    private val pse: PseSearchService,
    private val vision: VisionWebDetectionClient
) {
    private val uploadValidator = ImageUploadValidator(props.vision.maxUploadBytes)

    companion object {
        private val bad = setOf("LIKELY", "VERY_LIKELY")

        fun isUnsafeSafeSearch(ann: Map<String, String>): Boolean {
            val adult = ann["adult"]?.uppercase() ?: "UNKNOWN"
            val violence = ann["violence"]?.uppercase() ?: "UNKNOWN"
            val racy = ann["racy"]?.uppercase() ?: "UNKNOWN"
            return adult in bad || violence in bad || racy == "VERY_LIKELY"
        }
    }

    fun byText(q: String, size: Int = 10): Pair<List<ImageHit>, String?> =
        pse.searchImages(q, size)

    fun byUpload(file: MultipartFile): Pair<List<ImageHit>, String?> {
        if (!props.vision.enabled || props.vision.apiKey.isBlank()) {
            return emptyList<ImageHit>() to "VISION_UNAVAILABLE"
        }
        uploadValidator.requireValid(file)
        val base64 = Base64.getEncoder().encodeToString(file.bytes)
        return vision.detect(contentBase64 = base64, imageUri = null)
    }

    fun byUrl(imageUrl: String): Pair<List<ImageHit>, String?> {
        if (!props.vision.enabled || props.vision.apiKey.isBlank()) {
            return emptyList<ImageHit>() to "VISION_UNAVAILABLE"
        }
        val u = imageUrl.trim()
        require(u.startsWith("http://") || u.startsWith("https://")) { "URL invalida" }
        return vision.detect(contentBase64 = null, imageUri = u)
    }
}
