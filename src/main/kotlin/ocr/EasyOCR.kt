package com.score.ocr

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class EasyOCR() {
    private val gson = Gson()
    private val logger = KotlinLogging.logger {}

    companion object {
        val appDataPath: String? = System.getenv("LOCALAPPDATA")
        val pythonFile = File(appDataPath, "/aion2scf/python/ocr_script.py")
    }

    init {
        if(!pythonFile.exists()) {
            pythonFile.parentFile?.mkdirs()
            pythonFile.writeText(ImagePythonScript.pythonScript)
        }
    }

    suspend fun recognizeText(imagePath: String): List<OcrResult> = withContext(Dispatchers.IO) {
        try {
            // Python 스크립트 실행
            val processBuilder = ProcessBuilder(
                "python",
                pythonFile.absolutePath,
                imagePath
            )

            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()

            // 결과 읽기
            val output = process.inputStream.bufferedReader(Charsets.UTF_8).use {
                it.readText()
            }

            // 프로세스 종료 대기
            val exitCode = process.waitFor()

            if (exitCode != 0) {
                logger.error { "Python 스크립트 실행 실패: $output" }
                return@withContext emptyList()
            }

            // JSON 파싱
            val type = object : TypeToken<List<OcrResult>>() {}.type
            gson.fromJson(output, type)
        } catch (e: Exception) {
            logger.error(e) { "OCR 실행 중 오류" }
            emptyList()
        }
    }
}