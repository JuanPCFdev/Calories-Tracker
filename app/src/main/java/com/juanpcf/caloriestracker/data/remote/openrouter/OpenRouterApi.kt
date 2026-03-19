package com.juanpcf.caloriestracker.data.remote.openrouter

import com.juanpcf.caloriestracker.data.remote.openrouter.dto.ChatRequest
import com.juanpcf.caloriestracker.data.remote.openrouter.dto.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun analyzeFood(@Body request: ChatRequest): ChatResponse
}
