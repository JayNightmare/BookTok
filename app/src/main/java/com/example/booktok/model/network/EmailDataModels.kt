package com.example.booktok.model.network

data class EmailRequest(
    val personalizations: List<Personalization>,
    val from: EmailAddress,
    val subject: String,
    val content: List<EmailContent>
)

data class Personalization(
    val to: List<EmailAddress>
)

data class EmailAddress(
    val email: String,
    val name: String? = null
)

data class EmailContent(
    val type: String,
    val value: String
)
