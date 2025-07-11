package com.ivy.sms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.data.db.dao.read.SettingsDao
import com.ivy.data.model.SmsModel
import com.ivy.data.repository.SmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.todayIn
import javax.inject.Inject

@HiltViewModel
internal class SmsViewModel @Inject constructor(
    private val smsRepository: SmsRepository,
    private val settingsDao: SettingsDao
) : ViewModel() {

    private val _state = MutableStateFlow<SmsScreenState>(SmsScreenState())
    val state: StateFlow<SmsScreenState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val smsModels = smsRepository.readSms(getCurrentDayStart())
            val baseCurrency = settingsDao.findFirst().currency
            _state.update { s ->
                s.copy(
                    items = smsModels,
                    baseCurrency = baseCurrency,
                )
            }
        }
    }

    private fun getCurrentDayStart(): Instant {
        val today = kotlinx.datetime.Clock.System.todayIn(TimeZone.currentSystemDefault())
        val startOfDay = today.atTime(0, 0, 0)
        return startOfDay.toInstant(TimeZone.currentSystemDefault())
    }
}

internal data class SmsScreenState(
    val items: List<SmsModel> = emptyList(),
    val baseCurrency: String = "",
    val isLoading: Boolean = false,
    val isError: Boolean = false,
)