package com.ivy.sms

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ivy.base.legacy.Theme
import com.ivy.base.model.TransactionType
import com.ivy.data.model.SmsModel
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.design.l1_buildingBlocks.IvyText
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.ui.component.transaction.TypeAmountCurrency
import com.ivy.legacy.utils.formatNicelyWithTime
import com.ivy.navigation.EditTransactionScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.components.IvyButton
import com.ivy.wallet.ui.theme.components.IvyToolbar
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Clock

@Composable
fun SmsExpensesScreen() {
    val viewModel = screenScopedViewModel<SmsViewModel>()
    val state by viewModel.state.collectAsState()
    val sideEffect by viewModel.sideEffect.collectAsState(SmsSideEffect.None)
    val navigation = navigation()

    SmsScreenContent(
        state,
        viewModel::onSmsReadPermissionResult,
        viewModel::onSmsModelClicked,
        navigation::back,
    )

    LaunchedEffect(viewModel) {
        viewModel.load()
    }

    when (sideEffect) {
        is SmsSideEffect.OpenEditTransaction ->
            navigation.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = TransactionType.EXPENSE,
                    accountId = (sideEffect as SmsSideEffect.OpenEditTransaction).accountId
                )
            )
        else -> {}
    }
}

@Composable
private fun SmsScreenContent(
    state: SmsScreenState,
    onPermissionResult: (Boolean) -> Unit,
    onSmsModelClicked: (SmsListItem.Sms) -> Unit,
    onBackClicked: () -> Unit,
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onPermissionResult
    )

    Column(
        modifier = Modifier.systemBarsPadding()
    ) {
        IvyToolbar(onBack = onBackClicked) {
            IvyText(
                modifier = Modifier.padding(end = 16.dp),
                text = "Транзакции из смс",
                typo = UI.typo.b1.copy(
                    color = UI.colors.pureInverse
                )
            )
        }
        when (state.isPermissionGranted) {
            null -> Loading(modifier = Modifier.fillMaxSize())
            false -> NoPermissionGranted(
                requestPermission = { permissionLauncher.launch(Manifest.permission.READ_SMS) }
            )

            true -> SmsTransactions(
                items = state.items,
                baseCurrency = state.baseCurrency,
                openEditTransaction = onSmsModelClicked
            )
        }
    }
}

@Composable
fun Loading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = UI.colors.primary)
    }
}

@Composable
fun NoPermissionGranted(
    modifier: Modifier = Modifier,
    requestPermission: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        IvyText(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = "Необходимо разрешить доступ к чтению смс, чтобы иметь возможность прочитать ваши транзакции",
            typo = UI.typo.b2.copy(
                color = UI.colors.pureInverse
            ),
        )
        IvyButton(
            text = "Разрешить",
            onClick = requestPermission,
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun SmsTransactions(
    items: ImmutableList<SmsListItem>?,
    baseCurrency: String,
    modifier: Modifier = Modifier,
    openEditTransaction: (SmsListItem.Sms) -> Unit
) {
    if (items.isNullOrEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            IvyText(
                text = "Пусто",
                typo = UI.typo.b2.copy(
                    color = UI.colors.pureInverse
                )
            )
        }
    } else {
        LazyColumn(modifier = modifier) {
            items(
                items = items as List<SmsListItem>,
                key = { item: SmsListItem -> item.getKey() },
                contentType = { item: SmsListItem -> item.javaClass },
            ) { item: SmsListItem ->
                when (item) {
                    is SmsListItem.Sms ->
                        SmsTransactionItem(
                            smsModel = item,
                            baseCurrency = baseCurrency,
                            onClick = {
                                openEditTransaction(item)
                            }
                        )

                    is SmsListItem.DateSeparator ->
                        DateSeparator(
                            modifier = Modifier.padding(start = 12.dp),
                            date = item.date
                        )
                }

            }
        }
    }
}

@Composable
private fun DateSeparator(
    date: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        IvyText(
            text = date,
            typo = UI.typo.h1.copy(
                color = UI.colors.pureInverse
            )
        )
    }
}

@Composable
private fun SmsTransactionItem(
    smsModel: SmsListItem.Sms,
    baseCurrency: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(UI.shapes.r4)
            .clickable {
                onClick()
            }
            .background(UI.colors.medium, UI.shapes.r4)
            .testTag("sms_transaction_item")
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = smsModel.date.formatNicelyWithTime().uppercase(),
            style = UI.typo.nC.style(
                color = Gray,
                fontWeight = FontWeight.Bold
            )
        )

        if (smsModel.consumer.isNotBlank()) {
            Text(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                text = smsModel.consumer,
                style = UI.typo.b1.style(
                    fontWeight = FontWeight.ExtraBold,
                    color = UI.colors.pureInverse
                )
            )
        }

        if (smsModel.consumer.isBlank()) {
            Spacer(Modifier.height(16.dp))
        }

        TypeAmountCurrency(
            transactionType = TransactionType.EXPENSE,
            dueDate = null,
            currency = baseCurrency,
            amount = smsModel.amount
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Preview
@Composable
private fun PreviewItem() {
    IvyWalletPreview(Theme.LIGHT) {
        SmsScreenContent(
            state = SmsScreenState(
                isPermissionGranted = true,
                items = persistentListOf(
                    SmsListItem.DateSeparator("12.03.1997"),
                    SmsListItem.Sms(
                        id = "1234",
                        cardLastDigits = "3456",
                        date = Clock.System.now(),
                        amount = 123.45,
                        consumer = "ATM PBT"
                    )
                )
            ),
            onPermissionResult = {},
            onSmsModelClicked = {},
            onBackClicked = {}
        )
    }
}
