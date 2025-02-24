package com.example.booktok.model.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Email API
object EmailApi {
    // Base URL for the SendGrid API
    private const val BASE_URL = "https://api.sendgrid.com/"

    // Retrofit instance with the base URL and Gson converter
    val retrofitService: EmailApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmailApiService::class.java)
    }
}