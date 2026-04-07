package com.juanpcf.caloriestracker.data.remote.openrouter.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    @SerialName("id") val id: String = "",
    @SerialName("model") val model: String = "",
    @SerialName("choices") val choices: List<Choice> = emptyList(),
    @SerialName("error") val error: OpenRouterError? = null
)

@Serializable
data class OpenRouterError(
    @SerialName("message") val message: String = "",
    @SerialName("code") val code: Int? = null
)

@Serializable
data class Choice(
    @SerialName("index") val index: Int = 0,
    @SerialName("message") val message: MessageContent,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class MessageContent(
    @SerialName("role") val role: String = "",
    @SerialName("content") val content: String = "",
    @SerialName("reasoning_content") val reasoningContent: String? = null
)
