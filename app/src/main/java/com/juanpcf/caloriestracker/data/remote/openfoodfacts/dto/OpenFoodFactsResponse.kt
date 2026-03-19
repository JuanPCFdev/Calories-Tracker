package com.juanpcf.caloriestracker.data.remote.openfoodfacts.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenFoodFactsResponse(
    @SerialName("status") val status: Int = 0,
    @SerialName("product") val product: ProductDto? = null
)

@Serializable
data class ProductDto(
    @SerialName("product_name") val productName: String? = null,
    @SerialName("brands") val brands: String? = null,
    @SerialName("serving_quantity") val servingQuantity: Double? = null,
    @SerialName("nutriments") val nutriments: NutrimentsDto? = null
)

@Serializable
data class NutrimentsDto(
    @SerialName("energy-kcal_100g") val energyKcalPer100g: Double? = null,
    @SerialName("energy-kcal_serving") val energyKcalPerServing: Double? = null,
    @SerialName("proteins_100g") val proteinPer100g: Double? = null,
    @SerialName("proteins_serving") val proteinPerServing: Double? = null,
    @SerialName("carbohydrates_100g") val carbsPer100g: Double? = null,
    @SerialName("carbohydrates_serving") val carbsPerServing: Double? = null,
    @SerialName("fat_100g") val fatPer100g: Double? = null,
    @SerialName("fat_serving") val fatPerServing: Double? = null,
    @SerialName("fiber_100g") val fiberPer100g: Double? = null,
    @SerialName("sugars_100g") val sugarsPer100g: Double? = null
)
