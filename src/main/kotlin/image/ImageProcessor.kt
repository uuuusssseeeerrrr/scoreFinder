package com.score.image

import com.score.ocr.EasyOCR
import com.score.ocr.OcrResult
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.io.File

class ImageProcessor {
    val logger = KotlinLogging.logger {}
    val resultKeys = mutableSetOf<String>()
    val userSet = mutableSetOf<String>()

    private val _statusFlow = MutableSharedFlow<String>()
    val statusFlow: SharedFlow<String> = _statusFlow.asSharedFlow()

    fun start(intervalMs: Long = 500): Flow<Map<String, List<OcrResult>>> = flow {
        while (true) {
            try {
                val results = process()
                if (results.isNotEmpty()) {
                    if (resultKeys != userSet) {
                        userSet.clear()
                        userSet.addAll(resultKeys)
                        emit(results)
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "모니터링 오류" }
            }

            delay(intervalMs)
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun process(): MutableMap<String, List<OcrResult>> {
        val titleImageFile = ImageCapture.titleCapture()
        lateinit var partyImageFile: File
        val easyOcr = EasyOCR()
        val titleOcrList = easyOcr.recognizeText(titleImageFile.absolutePath)

        if (titleOcrList.isNotEmpty()) {
            if (titleOcrList[0].text.contains("성역")) {
                val results = mutableMapOf<String, List<OcrResult>>()
                resultKeys.clear()

                _statusFlow.emit("partyCapturingStart")
                partyImageFile = ImageCapture.partyCapture("성역")
                val partyOcrList = easyOcr.recognizeText(partyImageFile.absolutePath)
                val filteredOcrList =
                    partyOcrList.filter { it.text.contains("파티") || it.text.contains("[") || it.text.contains("]") }
                val partyOneBbox = filteredOcrList.firstOrNull { it.text.contains("파티 1") }?.bbox[0]
                val partyTwoBbox = filteredOcrList.firstOrNull { it.text.contains("파티 2") }?.bbox[0]

                if (partyOneBbox != null) {
                    results["party1"] = filteredOcrList.filter {
                        filterText(partyOneBbox[0], it.bbox[0][0], it.text)
                    }
                    .take(4)
                    .map { result ->
                        val text = textProcess(result.text).replace(" ", "")
                        resultKeys.add(text)
                        result.copy(text = text)
                    }
                }

                if (partyTwoBbox != null) {
                    results["party2"] =
                        filteredOcrList.filter {
                            filterText(partyTwoBbox[0], it.bbox[0][0], it.text)
                        }
                        .take(4)
                        .map { result ->
                            val text = textProcess(result.text).replace(" ", "")
                            resultKeys.add(text)
                            result.copy(text = text)
                        }
                }

                _statusFlow.emit("partyCapturingDone")
                return results
            }
        }

        return mutableMapOf()
    }

    private fun filterText(standardX: Double, targetX: Double, txt: String): Boolean {
        val pixel = 100
        val excludedTexts = listOf("파티 정보", "파티 1", "파티 2")
        val xMin = standardX - pixel
        val xMax = standardX + pixel
        return targetX in xMin..xMax && excludedTexts.none { text -> txt.contains(text) }
    }

    private fun textProcess(text: String): String {
        val wrongStringMap = mapOf(
            "화이" to "콰이",
            "과이" to "콰이",
            "지길" to "지켈",
            "네온" to "네몬",
            "로" to "크로",
            "드" to "루드",
            "인도" to "인드",
            "무난" to "무닌",
        )

        return when {
            text.count { it == '[' } == 1 && !text.contains("]") -> {
                val serverPart = text.substringAfter('[')
                text.substringBefore('[') + "[" + (wrongStringMap[serverPart] ?: serverPart) + "]"
            }

            text.count { it == ']' } == 1 && !text.contains("[") -> {
                val index = text.indexOf(']')
                val serverPart = text.substring(index - 2, index)
                text.substring(0, index - 2) + "[" + (wrongStringMap[serverPart] ?: serverPart) + "]"
            }

            text.contains("[") && text.contains("]") -> {
                val serverPart = text.substringAfter('[').substringBefore(']')
                text.substringBefore('[') + "[" + (wrongStringMap[serverPart] ?: serverPart) + "]"
            }

            else -> text
        }
    }
}