package com.ivy.data.repository

import com.ivy.data.model.SmsModel
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

interface SmsRepository {

    suspend fun readSms(fromDate: Instant): List<SmsModel>
}