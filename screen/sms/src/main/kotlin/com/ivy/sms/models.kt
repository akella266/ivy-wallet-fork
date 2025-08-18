package com.ivy.sms

import kotlinx.datetime.Instant

sealed interface SmsListItem {

    data class DateSeparator(val date: String) : SmsListItem

    data class Sms(
        val id: String,
        val cardLastDigits: String,
        val date: Instant,
        val amount: Double,
        val consumer: String,
    ) : SmsListItem
}

fun SmsListItem.getKey(): String =
    when (this) {
        is SmsListItem.DateSeparator -> date
        is SmsListItem.Sms -> id
    }