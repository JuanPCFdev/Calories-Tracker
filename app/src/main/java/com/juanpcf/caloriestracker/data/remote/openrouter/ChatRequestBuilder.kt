package com.juanpcf.caloriestracker.data.remote.openrouter

import com.juanpcf.caloriestracker.data.remote.openrouter.dto.ChatRequest
import com.juanpcf.caloriestracker.data.remote.openrouter.dto.ContentPart
import com.juanpcf.caloriestracker.data.remote.openrouter.dto.ImageUrl
import com.juanpcf.caloriestracker.data.remote.openrouter.dto.Message
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

private val SYSTEM_PROMPT = """
You are a nutrition expert AI. The user will send you an image of food.
Analyze the image and respond ONLY with a valid JSON object in this exact format, no other text:
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

object ChatRequestBuilder {
    private val json = Json { encodeDefaults = true }

    fun buildFoodAnalysisRequest(base64Image: String): ChatRequest {
        val systemMessage = Message(
            role = "system",
            content = json.encodeToJsonElement(SYSTEM_PROMPT)
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
        return ChatRequest(messages = listOf(systemMessage, userMessage))
    }
}
