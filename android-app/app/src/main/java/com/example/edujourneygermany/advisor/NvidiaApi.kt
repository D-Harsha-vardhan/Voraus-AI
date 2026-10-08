package com.example.edujourneygermany.advisor

import com.google.gson.JsonElement
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class NvidiaMessage(val role: String, val content: String)
data class NvidiaRequest(val model: String, val messages: List<NvidiaMessage>)

interface NvidiaApi {
    @POST("v1/chat/completions")
    suspend fun sendMessage(
        @Header("Authorization") authHeader: String,
        @Body request: NvidiaRequest
    ): JsonElement
}

object RetrofitClient {
    private const val BASE_URL = "https://integrate.api.nvidia.com/"

    val nvidiaApi: NvidiaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NvidiaApi::class.java)
    }
}
