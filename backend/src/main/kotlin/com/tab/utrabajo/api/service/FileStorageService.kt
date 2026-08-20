package com.tab.utrabajo.api.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID

@Service
class FileStorageService(
    @Value("\${app.upload-directory:./data/uploads}") uploadDirectory: String,
    @Value("\${app.public-base-url:http://10.0.2.2:8080}") private val publicBaseUrl: String,
) {
    private val root = Path.of(uploadDirectory).toAbsolutePath().normalize().also(Files::createDirectories)

    fun store(file: MultipartFile, category: String, allowedTypes: Set<String>): String {
        require(!file.isEmpty) { "El archivo está vacío" }
        require(file.contentType in allowedTypes) { "Tipo de archivo no permitido" }

        val extension = when (file.contentType) {
            "application/pdf" -> ".pdf"
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            else -> ".jpg"
        }
        val directory = root.resolve(category).normalize()
        require(directory.startsWith(root)) { "Ruta de almacenamiento inválida" }
        Files.createDirectories(directory)
        val target = directory.resolve("${UUID.randomUUID()}$extension")
        file.inputStream.use { Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING) }
        val relative = root.relativize(target).toString().replace('\\', '/')
        return "${publicBaseUrl.trimEnd('/')}/uploads/$relative"
    }
}
