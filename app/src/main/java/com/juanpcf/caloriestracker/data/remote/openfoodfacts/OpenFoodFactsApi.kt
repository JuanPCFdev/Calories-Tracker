package com.juanpcf.caloriestracker.data.remote.openfoodfacts

import com.juanpcf.caloriestracker.data.remote.openfoodfacts.dto.OpenFoodFactsResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {
    @GET("api/v2/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): OpenFoodFactsResponse
}
