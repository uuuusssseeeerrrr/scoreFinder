package com.score.webview

import com.score.aionTool.A2ToolParser
import com.score.ocr.OcrResult
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.application.Application
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import netscape.javascript.JSObject
import kotlin.system.exitProcess

class Browser : Application() {
    private val logger = KotlinLogging.logger {}
    private var bridge = JSBridge()
    lateinit var engine: WebEngine
    private val ready = CompletableDeferred<Unit>()

    init {
        getA2ToolData()
    }

    override fun start(stage: Stage) {
        stage.setOnCloseRequest {
            exitProcess(0)
        }

        val webView = WebView()
        engine = webView.engine
        engine.load(javaClass.getResource("/index.html")?.toExternalForm())

        bridge.setProperties(stage, hostServices = this.hostServices)

        engine.loadWorker.stateProperty().addListener { _, _, newState ->
            if (newState == Worker.State.SUCCEEDED) {
                val window = engine.executeScript("window") as JSObject
                window.setMember("javaBridge", bridge)

                engine.executeScript(
                    EngineJsScript.logScript
                )

                if (!ready.isCompleted) ready.complete(Unit)
            } else if (newState == Worker.State.FAILED) {
                logger.error { "Failed to load web page: ${engine.loadWorker.exception}" }
            }
        }

        engine.load(javaClass.getResource("/web/index.html")?.toExternalForm())

        val scene = Scene(webView, 600.0, 1000.0)
        scene.fill = Color.TRANSPARENT

        try {
            val pageField = engine.javaClass.getDeclaredField("page")
            pageField.isAccessible = true
            val page = pageField.get(engine)

            val setBgMethod = page.javaClass.getMethod("setBackgroundColor", Int::class.javaPrimitiveType)
            setBgMethod.isAccessible = true
            setBgMethod.invoke(page, 0)
        } catch (e: Exception) {
            logger.error(e) { "리플렉션 실패" }
        }

        stage.initStyle(StageStyle.TRANSPARENT)
        stage.scene = scene
        stage.isAlwaysOnTop = true
        stage.title = "Aion2 ScoreFinder"

        val primaryScreen = Screen.getPrimary()
        val bounds = primaryScreen.visualBounds

        when {
            bounds.width > 2200 -> {
                stage.x = bounds.minX + bounds.width * 0.02
                stage.y = bounds.minY + bounds.height * 0.32
            }
        }

        stage.show()
    }

    fun sendOcrData(ocrResult: Map<String, List<OcrResult>>) {
        // FX 스레드가 아니면 FX 스레드로 넘기기
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater { sendOcrData(ocrResult) }
            return
        }

        // 엔진/페이지 준비 전이면 데이터 유실 방지를 위해 대기 후 실행
        if (!ready.isCompleted) {
            ready.invokeOnCompletion {
                Platform.runLater { sendOcrData(ocrResult) }
            }
            return
        }

        if (::engine.isInitialized) {
            val json = Json.encodeToString(ocrResult).replace("`", "")
//            val json = """{"party1":[{"text":"한밤[콰이]","confidence":0.9985337},{"text":"연한커피[이스]","confidence":0.88061714},{"text":"턱키루키[울고]","confidence":0.47957313},{"text":"Oioi[울고]","confidence":0.8643941}],"party2":[{"text":"EunLee[과이]","confidence":0.8025098},{"text":"멘슬[과이]","confidence":0.49863476},{"text":"민초아[트리]","confidence":0.45212522}]}"""
//            val json = """{"party1":[{"text":"한밤[콰이]","confidence":0.9985337}]}"""

            logger.info { "OcrData: $json" }
            engine.executeScript(
                """
                    (function() {
                        window.dispatchEvent(new CustomEvent('sendOcrData', { 
                            detail: JSON.parse(`$json`) 
                        }));
                    })();
                """.trimIndent()
            )
        }
    }

    fun getA2ToolData() {
        CoroutineScope(Dispatchers.Default).launch {
            bridge.events.collect { s ->
                val data = Json.parseToJsonElement(s).jsonArray

                val results = data.map { userName ->
                    async {
                        A2ToolParser.apiCall(userName.jsonPrimitive.content)
                    }
                }.awaitAll()

                Platform.runLater {
                    engine.executeScript(
                        """
                            (function() {
                                window.dispatchEvent(new CustomEvent('sendToolData', { 
                                    detail: JSON.parse(`${Json.encodeToString(results)}`) 
                                }));
                            })();
                        """.trimIndent()
                    )
                }
            }
        }
    }

    fun sendStatus(status: String) {
        Platform.runLater {
            engine.executeScript(
                """
                            (function() {
                                window.dispatchEvent(new CustomEvent('sendStatus', { 
                                    detail: "$status" 
                                }));
                            })();
                        """.trimIndent()
            )
        }
    }
}