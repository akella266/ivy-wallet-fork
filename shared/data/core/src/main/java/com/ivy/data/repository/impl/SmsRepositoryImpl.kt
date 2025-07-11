package com.ivy.data.repository.impl

import android.content.ContentResolver
import androidx.core.net.toUri
import com.ivy.data.model.SmsModel
import com.ivy.data.repository.SmsRepository
import com.ivy.data.repository.mapper.SmsMapper
import kotlinx.coroutines.channels.Channel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

private const val BODY_COLUMN_NAME = "body"
private const val ADDRESS_COLUMN_NAME = "address"
private const val ID_COLUMN_NAME = "id"

internal class SmsRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    private val smsMapper: SmsMapper,
): SmsRepository {

    override suspend fun readSms(fromDate: Instant): List<SmsModel> {
        val sms = mutableListOf<SmsModel>()
        val uri = "content://sms/inbox".toUri()
        contentResolver.query(
            uri,
            arrayOf(ID_COLUMN_NAME, BODY_COLUMN_NAME, ADDRESS_COLUMN_NAME),
            "date > ?",
            arrayOf(fromDate.toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays().toString()),
            null
        ).use { cursor ->
            cursor ?: return@use

            val bodyIndex = cursor.getColumnIndex(BODY_COLUMN_NAME)
            val addressIndex = cursor.getColumnIndex(ADDRESS_COLUMN_NAME)
            val idIndex = cursor.getColumnIndex(ID_COLUMN_NAME)

            while (cursor.moveToNext()) {
                val body = cursor.getString(bodyIndex)
                val address = cursor.getString(addressIndex)
                val id = cursor.getString(idIndex)

                smsMapper.map(id, body, address)?.let { model ->
                    sms.add(model)
                }
            }
        }

        return sms
    }
}