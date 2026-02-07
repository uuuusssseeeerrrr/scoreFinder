package com.score.webview

import com.score.scoreFinder.BuildConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.application.HostServices
import javafx.application.Platform
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class JSBridge {
    private lateinit var stage: Stage
    private lateinit var hostServices: HostServices
    private val logger = KotlinLogging.logger {}
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    fun setProperties(stage: Stage, hostServices: HostServices) {
        this.stage = stage
        this.hostServices = hostServices
    }

    fun moveWindow(x: Double, y: Double) {
        stage.x = x
        stage.y = y
    }

    fun openBrowser(url: String) {
        try {
            hostServices.showDocument(url)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exitApp() {
        Platform.exit()
        exitProcess(0)
    }

    fun printLog(message: String) {
        logger.info { message }
    }

    fun getCurrentVersion(): String {
        return BuildConfig.APP_VERSION
    }

    fun getLatestVersion(): String {
        return GitReleaseParser.currentVersion.ifEmpty { BuildConfig.APP_VERSION }
    }

    fun searchUser(dataStr: String) {
        logger.info { "searchUser: $dataStr" }
        CoroutineScope(Dispatchers.JavaFx).launch {
            _events.emit(dataStr)  // JSON 문자열 그대로 emit
        }
    }
}