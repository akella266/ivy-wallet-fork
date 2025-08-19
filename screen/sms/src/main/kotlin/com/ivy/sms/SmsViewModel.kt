package com.ivy.sms

import android.Manifest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.data.db.dao.read.SettingsDao
import com.ivy.data.model.SmsModel
import com.ivy.data.repository.SmsRepository
import com.ivy.wallet.domain.action.account.AccountsAct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
internal class SmsViewModel @Inject constructor(
    private val smsRepository: SmsRepository,
    private val settingsDao: SettingsDao,
    private val permissionChecker: PermissionChecker,
    private val accountsAct: AccountsAct,
) : ViewModel() {

    private val _state = MutableStateFlow(SmsScreenState())
    val state: StateFlow<SmsScreenState> = _state.asStateFlow()

    private val _sideEffect = MutableSharedFlow<SmsSideEffect>()
    val sideEffect: Flow<SmsSideEffect> = _sideEffect.asSharedFlow()

    private val rusMonthNames = MonthNames(listOf(
        "января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября",
        "октября", "ноября", "декабря"
    ))

    private val dateFormater = DateTimeComponents.Format {
        dayOfMonth()
        char(' ')
        monthName(rusMonthNames)
    }

    fun load() {
        viewModelScope.launch {
            val result = permissionChecker.checkPermissionGranted(Manifest.permission.READ_SMS)

            if (result) {
                val smsModels = smsRepository.readSms(getCurrentDayStart())
                val smsItems = handleSmsModels(smsModels)
                val baseCurrency = settingsDao.findFirst().currency
                _state.update { s ->
                    s.copy(
                        items = smsItems,
                        baseCurrency = baseCurrency,
                        isPermissionGranted = true
                    )
                }
            } else {
                _state.update { s -> s.copy(isPermissionGranted = false)}
            }
        }
    }

    fun onSmsReadPermissionResult(result: Boolean) {
        _state.update { s ->
            s.copy(
                isPermissionGranted = result
            )
        }
    }

    fun onSmsModelClicked(item: SmsListItem.Sms) {
        viewModelScope.launch {
            val accounts = accountsAct()
            val accountForCard = accounts.find { acc -> acc.name.contains(item.cardLastDigits) }

            _sideEffect.tryEmit(
                SmsSideEffect.OpenEditTransaction(accountForCard?.id, item)
            )
        }
    }

    private fun handleSmsModels(models: List<SmsModel>): ImmutableList<SmsListItem> {
        val smsSeparatedByDate = models.groupBy(
            keySelector = { smsModel -> smsModel.date.format(dateFormater) },
            valueTransform = { smsModel ->
                SmsListItem.Sms(
                    smsModel.id,
                    smsModel.cardLastDigits,
                    smsModel.date,
                    smsModel.amount,
                    smsModel.consumer
                )
            }
        )

        return buildList<SmsListItem> {
            smsSeparatedByDate.entries.forEach { (date, smsModels) ->
                add(SmsListItem.DateSeparator(date))
                smsModels.forEach { model -> add(model) }
            }
        }.toImmutableList()
    }

    private fun getCurrentDayStart(): Instant {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val startOfDay = today.atTime(0, 0, 0)
        return startOfDay.toInstant(TimeZone.currentSystemDefault())
    }
}

internal data class SmsScreenState(
    val isPermissionGranted: Boolean? = null,
    val items: ImmutableList<SmsListItem>? = null,
    val baseCurrency: String = "",
    val isLoading: Boolean = false,
    val isError: Boolean = false,
)

internal sealed interface SmsSideEffect {

    data object None : SmsSideEffect

    data class OpenEditTransaction(
        val accountId: UUID?,
        val smsModel: SmsListItem.Sms,
    ): SmsSideEffect
}