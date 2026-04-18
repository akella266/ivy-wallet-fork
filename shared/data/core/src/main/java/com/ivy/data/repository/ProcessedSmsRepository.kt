package com.ivy.data.repository

import javax.inject.Singleton

@Singleton
interface ProcessedSmsRepository {

    suspend fun isProcessed(smsId: String): Boolean

    suspend fun getProcessedSmsIds(): Set<String>
}
