package com.example.mychatbotapp.api


import com.example.mychatbotapp.model.GeminiRequest
import com.example.mychatbotapp.model.GeminiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    @POST("models/gemini-1.5-flash:generateContent")
    fun sendMessage(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Call<GeminiResponse>
}
