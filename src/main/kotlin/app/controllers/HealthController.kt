package app.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

// permite comprobar si el servicio está funcionando correctamente
@RestController
class HealthController {
    @GetMapping("/health")
    fun health() = mapOf("status" to "Everything is OK")
}
