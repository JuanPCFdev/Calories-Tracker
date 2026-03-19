package com.juanpcf.caloriestracker.data.repository

import android.graphics.Bitmap
import com.juanpcf.caloriestracker.core.util.ImageUtils
import com.juanpcf.caloriestracker.data.local.dao.FoodCacheDao
import com.juanpcf.caloriestracker.data.local.entity.FoodCacheEntity
import com.juanpcf.caloriestracker.data.remote.openrouter.ChatRequestBuilder
import com.juanpcf.caloriestracker.data.remote.openrouter.OpenRouterApi
import com.juanpcf.caloriestracker.data.remote.openfoodfacts.OpenFoodFactsApi
import com.juanpcf.caloriestracker.data.remote.usda.UsdaApi
import com.juanpcf.caloriestracker.data.remote.usda.dto.UsdaNutrient
import com.juanpcf.caloriestracker.domain.model.Food
import com.juanpcf.caloriestracker.domain.model.FoodSource
import com.juanpcf.caloriestracker.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val foodCacheDao: FoodCacheDao,
    private val usdaApi: UsdaApi,
    private val openFoodFactsApi: OpenFoodFactsApi,
    private val openRouterApi: OpenRouterApi,
    private val usdaApiKey: String
) : FoodRepository {

    companion object {
        private const val CACHE_TTL_MILLIS = 24 * 60 * 60 * 1000L
    }

    override fun searchFoods(query: String): Flow<Result<List<Food>>> = flow {
        val now = Instant.now().toEpochMilli()
        val normalizedQuery = query.lowercase().trim()
        val cached = foodCacheDao.getCachedResults(normalizedQuery, now - CACHE_TTL_MILLIS)
        if (cached.isNotEmpty()) {
            emit(Result.success(cached.map { it.toDomain() }))
            return@flow
        }
        val result = runCatching {
            val response = usdaApi.searchFoods(query = query, apiKey = usdaApiKey)
            response.foods.map { item ->
                val nutrients = item.foodNutrients.associateBy { it.nutrientId }
                Food(
                    id = item.fdcId.toString(),
                    name = item.description,
                    brand = item.brandOwner,
                    calories = nutrients[UsdaNutrient.ENERGY_ID]?.value ?: 0.0,
                    protein = nutrients[UsdaNutrient.PROTEIN_ID]?.value ?: 0.0,
                    carbs = nutrients[UsdaNutrient.CARBS_ID]?.value ?: 0.0,
                    fat = nutrients[UsdaNutrient.FAT_ID]?.value ?: 0.0,
                    fiber = nutrients[UsdaNutrient.FIBER_ID]?.value,
                    sugar = nutrients[UsdaNutrient.SUGAR_ID]?.value,
                    servingSize = item.servingSize ?: 100.0,
                    servingUnit = item.servingSizeUnit ?: "g",
                    source = FoodSource.USDA
                )
            }.also { foods ->
                foodCacheDao.insertAll(foods.map { FoodCacheEntity.fromDomain(it, normalizedQuery, now) })
            }
        }
        emit(result)
    }

    override suspend fun getFoodByBarcode(barcode: String): Result<Food> = runCatching {
        foodCacheDao.getByBarcode(barcode)?.toDomain()?.let { return@runCatching it }
        val response = openFoodFactsApi.getProduct(barcode)
        if (response.status == 0 || response.product == null)
            throw NoSuchElementException("Product not found: $barcode")
        val p = response.product
        val n = p.nutriments
        val serving = p.servingQuantity ?: 100.0
        val food = Food(
            id = barcode, name = p.productName ?: "Unknown", brand = p.brands,
            calories = n?.energyKcalPerServing ?: ((n?.energyKcalPer100g ?: 0.0) * serving / 100.0),
            protein  = n?.proteinPerServing  ?: ((n?.proteinPer100g  ?: 0.0) * serving / 100.0),
            carbs    = n?.carbsPerServing    ?: ((n?.carbsPer100g    ?: 0.0) * serving / 100.0),
            fat      = n?.fatPerServing      ?: ((n?.fatPer100g      ?: 0.0) * serving / 100.0),
            fiber = n?.fiberPer100g?.times(serving / 100.0),
            sugar = n?.sugarsPer100g?.times(serving / 100.0),
            servingSize = serving, servingUnit = "g", barcode = barcode, source = FoodSource.OPEN_FOOD_FACTS
        )
        foodCacheDao.insert(FoodCacheEntity.fromDomain(food, null, Instant.now().toEpochMilli()))
        food
    }

    override suspend fun recognizeFoodFromImage(bitmap: Bitmap): Result<Food> = runCatching {
        val base64 = ImageUtils.bitmapToBase64(bitmap)
        val request = ChatRequestBuilder.buildFoodAnalysisRequest(base64)
        val response = openRouterApi.analyzeFood(request)
        val content = response.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("Empty OpenRouter response")
        val obj = Json.parseToJsonElement(content.trim()).jsonObject
        if (obj["error"]?.jsonPrimitive?.content == "UNRECOGNIZED")
            throw IllegalArgumentException("UNRECOGNIZED")
        Food(
            id = UUID.randomUUID().toString(),
            name = obj["name"]?.jsonPrimitive?.content ?: "Unknown Food",
            calories = obj["calories"]?.jsonPrimitive?.double ?: 0.0,
            protein  = obj["protein"]?.jsonPrimitive?.double  ?: 0.0,
            carbs    = obj["carbs"]?.jsonPrimitive?.double    ?: 0.0,
            fat      = obj["fat"]?.jsonPrimitive?.double      ?: 0.0,
            servingSize = obj["servingSize"]?.jsonPrimitive?.double ?: 100.0,
            servingUnit = obj["servingUnit"]?.jsonPrimitive?.content ?: "g",
            source = FoodSource.AI
        )
    }

    override suspend fun getFoodById(id: String): Food? = foodCacheDao.getById(id)?.toDomain()
}
