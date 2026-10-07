package com.example.edujourneygermany.advisor

import com.google.gson.JsonElement
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class WebhookRequest(val message: String)

interface DronaHqApi {
    @POST("webhook/1f6ac931-615a-4d7a-aa58-c714de559b06")
    suspend fun sendMessage(
        @Header("Authorization") authHeader: String,
        @Header("api-key") apiKeyHeader: String,
        @Body request: WebhookRequest
    ): JsonElement
}

object RetrofitClient {
    private const val BASE_URL = "https://agents-backend.dronahq.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    val dronaHqApi: DronaHqApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DronaHqApi::class.java)
    }
}
