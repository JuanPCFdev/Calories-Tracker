package com.juanpcf.caloriestracker.data.remote.openrouter.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ChatRequest(
    @SerialName("model") val model: String = "qwen/qwen3-235b-a22b",
    @SerialName("messages") val messages: List<Message>,
    @SerialName("max_tokens") val maxTokens: Int = 300
)

@Serializable
data class Message(
    @SerialName("role") val role: String,
    @SerialName("content") val content: JsonElement
)

@Serializable
data class ContentPart(
    @SerialName("type") val type: String,
    @SerialName("text") val text: String? = null,
    @SerialName("image_url") val imageUrl: ImageUrl? = null
)

@Serializable
data class ImageUrl(@SerialName("url") val url: String)
