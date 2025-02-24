package com.example.booktok.model.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object EmailApi {
    private const val BASE_URL = "https://api.sendgrid.com/"

    val retrofitService: EmailApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmailApiService::class.java)
    }
}