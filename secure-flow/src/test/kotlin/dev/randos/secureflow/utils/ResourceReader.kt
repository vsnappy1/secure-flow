package dev.randos.secureflow.utils

object ResourceReader {

    fun readFile(fileName: String, path: String = "dev/randos/secureflow/"): String {
        val classLoader = this::class.java.classLoader
        val resourcePath = "$path$fileName"
        val inputStream = classLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Resource file not found: $resourcePath")
        return inputStream.bufferedReader().use { it.readText() }
    }
}
