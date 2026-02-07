package com.score

import com.score.image.ImageProcessor
import com.score.ocr.OcrResult
import com.score.webview.Browser
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.application.Platform
import javafx.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    val logger = KotlinLogging.logger {}
    val ocrResult = mutableMapOf<String, List<OcrResult>>()
    val browser = Browser()
    val imageProcessor = ImageProcessor()

    runBlocking {
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            logger.error { "thread dead ${t.name}" }
            e.printStackTrace()
        }

        Platform.startup {
            browser.start(Stage())
        }

        launch(Dispatchers.IO) {
            imageProcessor.start().collect {
                ocrResult.clear()
                ocrResult.putAll(it)
                browser.sendOcrData(ocrResult)
            }
        }

        launch(Dispatchers.Default) {
            imageProcessor.statusFlow.collect { status ->
                logger.info { "Status: $status" }
                browser.sendStatus(status)
            }
        }
    }
}