package com.ivy.data.repository.impl

import com.ivy.data.db.dao.read.TransactionDao
import com.ivy.data.repository.ProcessedSmsRepository
import javax.inject.Inject

internal class ProcessedSmsRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : ProcessedSmsRepository {

    override suspend fun isProcessed(smsId: String): Boolean =
        transactionDao.findBySmsId(smsId) != null

    override suspend fun getProcessedSmsIds(): Set<String> =
        transactionDao.findAllSmsIds().toSet()
}
