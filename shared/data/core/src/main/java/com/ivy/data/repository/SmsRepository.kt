package com.ivy.data.repository

import com.ivy.data.model.SmsModel
import kotlinx.datetime.Instant
import javax.inject.Singleton

@Singleton
interface SmsRepository {

    suspend fun readSms(fromDate: Instant): List<SmsModel>
}