package com.example.booktok.model.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface EmailApiService {

    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer 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"  // Replace with actual API key
    )
    @POST("v3/mail/send")
    suspend fun sendEmail(
        @Body emailRequest: EmailRequest
    ): Response<Void>
}
