package com.example.edujourneygermany.advisor

import com.google.gson.JsonElement
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

data class DronaHqRequest(val query: String, val profile: Map<String, String>)

interface DronaHqApi {
    @POST
    suspend fun sendMessage(
        @Url url: String,
        @Header("api-key") apiKeyHeader: String,
        @Body request: DronaHqRequest
    ): JsonElement
}

object DronaHqClient {
    val api: DronaHqApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://dummy.com/") // Base URL is overridden by @Url
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DronaHqApi::class.java)
    }
}
