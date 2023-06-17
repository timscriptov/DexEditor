package com.mcal.common.utils

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

object FileHelper {
    fun readFile(path: String): String = FileInputStream(File(path)).readBytes().toString(StandardCharsets.UTF_8)

    fun readFile(file: File): String = FileInputStream(file).readBytes().toString(StandardCharsets.UTF_8)

    fun readFile(inputStream: InputStream): String = inputStream.readBytes().toString(StandardCharsets.UTF_8)

    fun writeText(fileName: String, fileContent: String) {
        File(fileName).writeText(fileContent)
    }

    fun writeText(file: File, fileContent: String) {
        file.writeText(fileContent)
    }
}