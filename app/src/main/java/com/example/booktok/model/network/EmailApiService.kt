package com.example.booktok.model.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// Email API interface
interface EmailApiService {

    // Header for the API request
    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer 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"  // Replace with actual API key
    )
    // POST request to send an email
    @POST("v3/mail/send")
    // Function to send an email
    suspend fun sendEmail(
        // Request body containing the email data
        @Body emailRequest: EmailRequest
    ): Response<Void>
}
