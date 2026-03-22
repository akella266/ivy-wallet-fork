package com.ivy.data.repository

interface ProcessedSmsRepository {

    suspend fun isProcessed(smsId: String): Boolean

    suspend fun getProcessedSmsIds(): Set<String>
}
