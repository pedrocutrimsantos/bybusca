package br.bybusca

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration::class])
class ByBuscaApplication

fun main(args: Array<String>) {
    runApplication<ByBuscaApplication>(*args)
}
