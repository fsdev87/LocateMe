package com.mustafafaraz.locateme.data.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Your deployed backend URL
    private const val BASE_URL = "https://locateme-backend.onrender.com/"

    // Logging interceptor to see API requests/responses in Logcat
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttp client with timeout settings
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)  // Longer timeout for Render cold starts
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Configure Gson to properly serialize arrays and handle nulls
    private val gson = GsonBuilder()
        .serializeNulls()  // Include null values in JSON
        .create()

    // Retrofit instance
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // API Service instance
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}