package com.juanpcf.caloriestracker.data.remote.openrouter

import com.juanpcf.caloriestracker.data.remote.openrouter.dto.ChatRequest
import com.juanpcf.caloriestracker.data.remote.openrouter.dto.ContentPart
import com.juanpcf.caloriestracker.data.remote.openrouter.dto.ImageUrl
import com.juanpcf.caloriestracker.data.remote.openrouter.dto.Message
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

private val RESPONSE_FORMAT = """
Respond ONLY with a valid JSON object in this exact format, no other text:
{
  "name": "food name in English",
  "servingSize": 100,
  "servingUnit": "g",
  "calories": 250.0,
  "protein": 8.5,
  "carbs": 35.0,
  "fat": 9.0
}
Rules:
- servingUnit must be one of: "g", "ml", "oz", "piece"
- All numeric values must be for the serving size specified
- If you cannot identify the food, respond with: {"error": "UNRECOGNIZED"}
- Never include explanations, markdown, or any text outside the JSON object
""".trimIndent()

private fun languageInstruction(languageTag: String) = when (languageTag) {
    "es" -> "Respond in Spanish. Use food names common in Colombia; use Colombian typical serving sizes when applicable."
    else -> "Respond in English."
}

private fun imageSystemPrompt(languageTag: String) = """
You are a nutrition expert AI. The user will send you an image of food or a nutrition label.
Analyze it and estimate (or read from the label) the nutritional information.
${languageInstruction(languageTag)}
$RESPONSE_FORMAT
""".trimIndent()

private fun textSystemPrompt(languageTag: String) = """
You are a nutrition expert AI. The user will send you the name of a food item or dish.
Estimate its nutritional information based on a standard serving.
${languageInstruction(languageTag)}
$RESPONSE_FORMAT
""".trimIndent()

object ChatRequestBuilder {
    private val json = Json { encodeDefaults = true }

    // OpenRouter tries each model in order on 429 / provider error. Max 3 models.
    // Vision-capable models only — image analysis requires multimodal support.
    private val MODELS_IMAGE = listOf(
        "qwen/qwen3.6-plus:free",
        "qwen/qwen2.5-vl-72b-instruct:free",
        "meta-llama/llama-4-scout:free"
    )

    // Text analysis — no vision requirement.
    private val MODELS_TEXT = listOf(
        "qwen/qwen3.6-plus:free",
        "qwen/qwen3-next-80b-a3b-instruct:free",
        "meta-llama/llama-3.3-70b-instruct:free"
    )

    fun buildFoodAnalysisRequest(base64Image: String, languageTag: String = "en"): ChatRequest {
        val systemMessage = Message(
            role = "system",
            content = json.encodeToJsonElement(imageSystemPrompt(languageTag))
        )
        val userContent = listOf(
            ContentPart(
                type = "image_url",
                imageUrl = ImageUrl("data:image/jpeg;base64,$base64Image")
            ),
            ContentPart(type = "text", text = "Analyze this food.")
        )
        val userMessage = Message(
            role = "user",
            content = json.encodeToJsonElement(userContent)
        )
        return ChatRequest(models = MODELS_IMAGE, messages = listOf(systemMessage, userMessage))
    }

    fun buildTextAnalysisRequest(foodName: String, languageTag: String = "en"): ChatRequest {
        val systemMessage = Message(
            role = "system",
            content = json.encodeToJsonElement(textSystemPrompt(languageTag))
        )
        val userMessage = Message(
            role = "user",
            content = json.encodeToJsonElement("Analyze this food: $foodName")
        )
        return ChatRequest(models = MODELS_TEXT, messages = listOf(systemMessage, userMessage))
    }
}
