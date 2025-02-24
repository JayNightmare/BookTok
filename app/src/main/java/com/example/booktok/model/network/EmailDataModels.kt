package com.example.booktok.model.network

// Email Request
data class EmailRequest(
    val personalizations: List<Personalization>,
    val from: EmailAddress,
    val subject: String,
    val content: List<EmailContent>
)

// Personalization
data class Personalization(
    val to: List<EmailAddress>
)

// Email Address
data class EmailAddress(
    val email: String,
    val name: String? = null
)

// Email Content
data class EmailContent(
    val type: String,
    val value: String
)
