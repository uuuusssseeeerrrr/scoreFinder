package com.score.aionTool.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserToolData(
    val name: String,
    val server: String,
    val combatPower: Int,
    val combatScore: Int,
)