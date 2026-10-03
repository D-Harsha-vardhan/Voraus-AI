package com.example.edujourneygermany.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // We would use an environment variable or config file for this in production
    private const val BASE_URL = "http://10.0.2.2:3000/" // Android emulator alias for localhost

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            // Here we'd add an OkHttpClient with an AuthInterceptor
            .build()
    }
}
