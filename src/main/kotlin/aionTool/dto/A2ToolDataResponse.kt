package com.score.aionTool.dto

import kotlinx.serialization.Serializable

@Serializable
data class A2ToolDataResponse(
    val data: List<A2ToolCharData>,
    val success: Boolean
)
