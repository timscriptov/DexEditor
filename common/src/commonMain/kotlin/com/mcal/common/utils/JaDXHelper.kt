package com.mcal.common.utils

import jadx.api.JadxDecompiler
import jadx.plugins.input.smali.SmaliInputPlugin
import java.io.File
import java.nio.file.Paths

object JaDXHelper {
    fun smali2java(content: String): String {
        val smaliFile = File.createTempFile("temp", ".smali")
        FileHelper.writeText(smaliFile, content)

        val outputFile = File.createTempFile("temp", ".java")

        JadxDecompiler().use { decompiler ->
            decompiler.addCustomLoad(
                SmaliInputPlugin().loadFiles(
                    arrayListOf(Paths.get(smaliFile.path))
                )
            )
            decompiler.load()
            decompiler.classes.forEach { javaClass ->
                FileHelper.writeText(outputFile, javaClass.code)
            }
        }
        val result = FileHelper.readFile(outputFile)
        smaliFile.delete()
        outputFile.delete()
        return result
    }
}
