package br.bybusca.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "bybusca")
data class ByBuscaProperties(
    var defaultTenant: String = "demo-escola",
    var jwt: JwtProps = JwtProps(),
    var pse: PseProps = PseProps(),
    var vision: VisionProps = VisionProps()
)

data class JwtProps(
    var secret: String = "change-me-bybusca-dev-secret-key-32b-min",
    var expirationMinutes: Long = 480
)

data class PseProps(
    var apiKey: String = "",
    var cx: String = "",
    var enabled: Boolean = false,
    var timeoutMs: Long = 2500
)

data class VisionProps(
    var enabled: Boolean = false,
    var apiKey: String = "",
    var timeoutMs: Long = 4000,
    var maxUploadBytes: Long = 5L * 1024 * 1024
)
