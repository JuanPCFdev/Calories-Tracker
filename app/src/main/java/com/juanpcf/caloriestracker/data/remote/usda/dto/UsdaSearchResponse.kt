package com.juanpcf.caloriestracker.data.remote.usda.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsdaSearchResponse(
    @SerialName("totalHits") val totalHits: Int = 0,
    @SerialName("foods") val foods: List<UsdaFoodItem> = emptyList()
)

@Serializable
data class UsdaFoodItem(
    @SerialName("fdcId") val fdcId: Int,
    @SerialName("description") val description: String,
    @SerialName("brandOwner") val brandOwner: String? = null,
    @SerialName("servingSize") val servingSize: Double? = null,
    @SerialName("servingSizeUnit") val servingSizeUnit: String? = null,
    @SerialName("foodNutrients") val foodNutrients: List<UsdaNutrient> = emptyList()
)

@Serializable
data class UsdaNutrient(
    @SerialName("nutrientId") val nutrientId: Int = 0,
    @SerialName("value") val value: Double? = null
) {
    companion object {
        const val ENERGY_ID = 1008
        const val PROTEIN_ID = 1003
        const val CARBS_ID = 1005
        const val FAT_ID = 1004
        const val FIBER_ID = 1079
        const val SUGAR_ID = 2000
    }
}
