package com.ivy.data.model

import kotlinx.datetime.Instant

data class SmsModel(
    val id: String,
    val cardLastDigits: String,
    val date: Instant,
    val amount: Double,
    val consumer: String,
)
