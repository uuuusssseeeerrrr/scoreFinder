package com.score.aionTool

import com.score.aionTool.ServerList.serverMap
import com.score.aionTool.dto.A2ToolCharData
import com.score.aionTool.dto.A2ToolDataResponse
import com.score.aionTool.dto.UserToolData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object A2ToolParser {
    private val client = HttpClient(CIO) {
//        install(Logging) {
//            logger = Logger.DEFAULT
//            level = LogLevel.ALL  // HEADERS, BODY, INFO, NONE 중 선택
//        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }

        defaultRequest {
            url("https://www.aion2tool.com/api/character/search-all-servers")

            headers {
                append(HttpHeaders.ContentType, "application/json; charset=utf-8")
                append(
                    "user-agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36"
                )
                append("Referer", "https://www.aion2tool.com")
                append("Origin", "https://www.aion2tool.com")
            }
        }
    }

    suspend fun apiCall(userData: String): UserToolData {
        val userSplitData = userData.split("[")
        val userName = userSplitData[0]
        val userServer = serverMap[userSplitData[1].replace("]", "")]
        var charData: A2ToolCharData? = null

        if (userServer != null) {
            val response = client.post {
                contentType(ContentType.Application.Json)

                setBody(
                    mapOf(
                        "keyword" to userName,
                        "race" to userServer[0].toString()
                    )
                )
            }.body<A2ToolDataResponse>()

            charData = response.data.find {
                it.nickname == userName && it.serverId == Integer.parseInt(userServer)
            }
        }

        return UserToolData(
            userName,
            charData?.serverName ?: "",
            charData?.combatPower?.toInt() ?: 0,
            charData?.combatScore?.toInt() ?: 0
        )
    }
}