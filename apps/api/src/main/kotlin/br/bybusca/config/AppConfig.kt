package br.bybusca.config

import br.bybusca.acl.AclService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
    @Bean
    fun aclService(): AclService = AclService()
}
