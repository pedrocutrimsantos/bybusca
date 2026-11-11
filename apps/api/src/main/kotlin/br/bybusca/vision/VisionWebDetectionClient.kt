package br.bybusca.vision

import br.bybusca.config.ByBuscaProperties
import br.bybusca.pse.EducationalContentFilter
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class VisionWebDetectionClient(
    private val props: ByBuscaProperties
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val client: RestClient by lazy {
        val factory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(props.vision.timeoutMs.toInt())
            setReadTimeout(props.vision.timeoutMs.toInt())
        }
        RestClient.builder()
            .baseUrl("https://vision.googleapis.com")
            .requestFactory(factory)
            .build()
    }

    fun detect(contentBase64: String?, imageUri: String?): Pair<List<ImageHit>, String?> {
        return try {
            val image = buildImagePayload(contentBase64, imageUri)
            val requestBody = mapOf(
                "requests" to listOf(
                    mapOf(
                        "image" to image,
                        "features" to listOf(
                            mapOf("type" to "WEB_DETECTION", "maxResults" to 10),
                            mapOf("type" to "SAFE_SEARCH_DETECTION")
                        )
                    )
                )
            )
            @Suppress("UNCHECKED_CAST")
            val body = client.post()
                .uri("/v1/images:annotate?key=${props.vision.apiKey}")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map::class.java) as Map<String, Any?>?

            val response = (body?.get("responses") as? List<Map<String, Any?>>)?.firstOrNull()
                ?: return emptyList<ImageHit>() to "VISION_UNAVAILABLE"

            val safeSearch = response["safeSearchAnnotation"] as? Map<String, Any?>
            if (safeSearch != null) {
                val ann = safeSearch.mapValues { (_, v) -> v?.toString().orEmpty() }
                if (ImageSearchService.isUnsafeSafeSearch(ann)) {
                    return emptyList<ImageHit>() to "IMAGE_BLOCKED"
                }
            }

            val webDetection = response["webDetection"] as? Map<String, Any?> ?: emptyMap()
            val hits = mutableListOf<ImageHit>()

            @Suppress("UNCHECKED_CAST")
            val pages = webDetection["pagesWithMatchingImages"] as? List<Map<String, Any?>> ?: emptyList()
            for (page in pages) {
                val pageUrl = page["url"]?.toString().orEmpty()
                if (pageUrl.isBlank()) continue
                val title = page["pageTitle"]?.toString().orEmpty()
                if (EducationalContentFilter.isBlocked(pageUrl)) continue
                hits.add(
                    ImageHit(
                        title = title,
                        pageUrl = pageUrl,
                        imageUrl = pageUrl,
                        thumbnailUrl = null,
                        score = 0.55 + EducationalContentFilter.scoreBoost(pageUrl),
                        source = "vision"
                    )
                )
            }

            @Suppress("UNCHECKED_CAST")
            val similar = webDetection["visuallySimilarImages"] as? List<Map<String, Any?>> ?: emptyList()
            for (img in similar) {
                val imageUrl = img["url"]?.toString().orEmpty()
                if (imageUrl.isBlank()) continue
                if (EducationalContentFilter.isBlocked(imageUrl)) continue
                hits.add(
                    ImageHit(
                        title = "",
                        pageUrl = imageUrl,
                        imageUrl = imageUrl,
                        thumbnailUrl = null,
                        score = 0.5 + EducationalContentFilter.scoreBoost(imageUrl),
                        source = "vision"
                    )
                )
            }

            hits.sortedByDescending { it.score } to null
        } catch (ex: Exception) {
            log.warn("Vision indisponível: {}", ex.message)
            emptyList<ImageHit>() to "VISION_UNAVAILABLE"
        }
    }

    private fun buildImagePayload(contentBase64: String?, imageUri: String?): Map<String, Any?> =
        when {
            !contentBase64.isNullOrBlank() -> mapOf("content" to contentBase64)
            !imageUri.isNullOrBlank() -> mapOf("source" to mapOf("imageUri" to imageUri))
            else -> throw IllegalArgumentException("Imagem ou URL obrigatoria")
        }
}
