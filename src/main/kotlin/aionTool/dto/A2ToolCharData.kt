package com.score.aionTool.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class A2ToolCharData(
    val nickname: String,
    @SerialName("server_id")
    val serverId: Int,
    @SerialName("combat_power")
    val combatPower: Double,
    @SerialName("combat_score")
    val combatScore: Double? = null,
    @SerialName("server_name")
    val serverName: String,
)
