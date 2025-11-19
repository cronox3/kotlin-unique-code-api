// build.gradle.kts
/*
plugins {
    kotlin("jvm") version "1.9.0"
    application
    kotlin("plugin.serialization") version "1.9.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.3.5")
    implementation("io.ktor:ktor-server-netty:2.3.5")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
}

application {
    mainClass.set("MainKt")
}
*/

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.Serializable
import java.security.MessageDigest

@Serializable
data class HashRequest(val text: String)

@Serializable
data class HashResponse(val hash: String)

fun stringToUniqueCode(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(input.toByteArray())

    var numericValue = 0L
    for (i in 0..7) {
        numericValue = (numericValue shl 8) or (hashBytes[i].toLong() and 0xFF)
    }
    numericValue = numericValue and Long.MAX_VALUE

    val base62Chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    val result = StringBuilder()
    var value = numericValue

    repeat(5) {
        result.append(base62Chars[(value % 62).toInt()])
        value /= 62
    }

    return result.toString()
}

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }

        routing {
            // Endpoint principal: recibe string, retorna hash
            post("/hash") {
                try {
                    val request = call.receive<HashRequest>()
                    val hash = stringToUniqueCode(request.text)
                    call.respond(HashResponse(hash))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Formato inválido. Usa: {\"text\":\"tu-string\"}")
                    )
                }
            }

            // Endpoint alternativo: parámetro en URL
            get("/hash/{text}") {
                val text = call.parameters["text"] ?: ""
                val hash = stringToUniqueCode(text)
                call.respond(HashResponse(hash))
            }

            // Página de inicio
            get("/") {
                call.respondText("""
                    🔐 API de Hash Único - Kotlin
                    ============================
                    
                    📌 Endpoint principal:
                    POST /hash
                    Body: {"text": "tu-string-aqui"}
                    Response: {"hash": "AB12x"}
                    
                    📌 Endpoint alternativo (GET):
                    GET /hash/tu-string-aqui
                    Response: {"hash": "AB12x"}
                    
                    ✅ Health check:
                    GET /health
                    
                    📚 Ejemplos de uso:
                    
                    curl:
                    curl -X POST https://tu-app.railway.app/hash \
                      -H "Content-Type: application/json" \
                      -d '{"text":"123456789"}'
                    
                    JavaScript:
                    fetch('https://tu-app.railway.app/hash', {
                      method: 'POST',
                      headers: {'Content-Type': 'application/json'},
                      body: JSON.stringify({text: '123456789'})
                    })
                    .then(r => r.json())
                    .then(data => console.log(data.hash));
                    
                    Navegador:
                    https://tu-app.railway.app/hash/123456789
                    
                """.trimIndent(), ContentType.Text.Plain)
            }

            // Health check para Railway
            get("/health") {
                call.respond(mapOf("status" to "OK"))
            }
        }
    }.start(wait = true)
}
