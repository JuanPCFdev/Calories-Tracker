package com.juanpcf.caloriestracker.data.remote.usda

import com.juanpcf.caloriestracker.data.remote.usda.dto.UsdaSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UsdaApi {
    @GET("fdc/v1/foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Query("api_key") apiKey: String,
        @Query("pageSize") pageSize: Int = 20,
        @Query("dataType") dataType: String = "Survey (FNDDS),SR Legacy"
    ): UsdaSearchResponse
}
