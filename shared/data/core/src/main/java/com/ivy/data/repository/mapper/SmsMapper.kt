package com.ivy.data.repository.mapper

import com.ivy.data.model.SmsModel
import kotlinx.datetime.Instant
import javax.inject.Inject

private const val PRIOR_SMS_REGEX = """Karta\s+\d+\*{3}(\d+)\s+(\d{2}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2})\. Oplata\s+([\d.]+)\s+BYN\. BLR\s+([^.]+)\."""
private const val PRIOR_SMS_ADDRESS_NAME = "Priorbank"

internal class SmsMapper @Inject constructor() {

    fun map(
        id: String,
        body: String,
        address: String,
    ): SmsModel? {
        val regex = Regex(PRIOR_SMS_REGEX)
        val match = regex.find(body) ?: return null

        if (address != PRIOR_SMS_ADDRESS_NAME) return null

        return if (match.value.isNotEmpty()) {
            val (cardNumber, dateTime, amountStr, subject) = match.destructured

            SmsModel(
                id = id,
                cardLastDigits = cardNumber,
                date = Instant.parse(dateTime),
                amount = amountStr.toDoubleOrNull() ?: 0.0,
                consumer = subject,
            )
        } else {
            null
        }
    }
}
