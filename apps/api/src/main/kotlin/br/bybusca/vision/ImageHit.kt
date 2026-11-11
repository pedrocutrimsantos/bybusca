package br.bybusca.vision

data class ImageHit(
    val title: String,
    val pageUrl: String,
    val imageUrl: String,
    val thumbnailUrl: String?,
    val score: Double,
    val source: String
)
