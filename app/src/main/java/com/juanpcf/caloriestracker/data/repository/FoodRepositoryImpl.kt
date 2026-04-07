package com.juanpcf.caloriestracker.data.repository

import android.graphics.Bitmap
import com.juanpcf.caloriestracker.core.util.ImageUtils
import com.juanpcf.caloriestracker.data.remote.openrouter.ChatRequestBuilder
import com.juanpcf.caloriestracker.data.remote.openrouter.OpenRouterApi
import com.juanpcf.caloriestracker.data.remote.openrouter.dto.ChatRequest
import com.juanpcf.caloriestracker.domain.model.AppPreferences
import com.juanpcf.caloriestracker.domain.model.Food
import com.juanpcf.caloriestracker.domain.model.FoodSource
import com.juanpcf.caloriestracker.domain.repository.AppPreferencesRepository
import com.juanpcf.caloriestracker.domain.repository.FoodRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val openRouterApi: OpenRouterApi,
    private val preferencesRepository: AppPreferencesRepository
) : FoodRepository {

    private val json = Json { ignoreUnknownKeys = true }

    // Models sometimes wrap JSON in markdown fences or prepend thinking tokens.
    // Extract the first complete {...} block from whatever the model returns.
    private fun extractJson(content: String): String {
        val start = content.indexOf('{')
        val end = content.lastIndexOf('}')
        if (start == -1 || end == -1 || end <= start)
            throw IllegalStateException("No JSON object found in AI response")
        return content.substring(start, end + 1)
    }

    override suspend fun recognizeFoodFromImage(bitmap: Bitmap): Result<Food> = runCatching {
        val lang = preferencesRepository.preferences.first().language.tag
        parseAiFood(ChatRequestBuilder.buildFoodAnalysisRequest(ImageUtils.bitmapToBase64(bitmap), lang))
    }

    override suspend fun recognizeFoodFromText(foodName: String): Result<Food> = runCatching {
        val lang = preferencesRepository.preferences.first().language.tag
        parseAiFood(ChatRequestBuilder.buildTextAnalysisRequest(foodName, lang))
    }

    private suspend fun parseAiFood(request: ChatRequest): Food {
        val response = openRouterApi.analyzeFood(request)
        if (response.error != null) {
            throw IllegalStateException("OpenRouter error ${response.error.code}: ${response.error.message}")
        }
        val message = response.choices.firstOrNull()?.message
            ?: throw IllegalStateException("Empty OpenRouter response")
        val content = message.content.takeIf { it.isNotBlank() }
            ?: message.reasoningContent?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Empty OpenRouter response")
        val obj = json.parseToJsonElement(extractJson(content)).jsonObject
        if (obj["error"]?.jsonPrimitive?.content == "UNRECOGNIZED")
            throw IllegalArgumentException("UNRECOGNIZED")
        return Food(
            id = UUID.randomUUID().toString(),
            name = obj["name"]?.jsonPrimitive?.content ?: "Unknown Food",
            calories = obj["calories"]?.jsonPrimitive?.double ?: 0.0,
            protein = obj["protein"]?.jsonPrimitive?.double ?: 0.0,
            carbs = obj["carbs"]?.jsonPrimitive?.double ?: 0.0,
            fat = obj["fat"]?.jsonPrimitive?.double ?: 0.0,
            servingSize = obj["servingSize"]?.jsonPrimitive?.double ?: 100.0,
            servingUnit = obj["servingUnit"]?.jsonPrimitive?.content ?: "g",
            source = FoodSource.AI
        )
    }
}
