package com.ivy.sms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.ui.component.transaction.TypeAmountCurrency
import com.ivy.legacy.utils.formatNicelyWithTime
import com.ivy.navigation.screenScopedViewModel
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.components.IvyToolbar
import kotlinx.datetime.Clock

@Composable
fun SmsExpensesScreen() {
    val viewModel = screenScopedViewModel<SmsViewModel>()
    val state by viewModel.state.collectAsState()
    SmsScreenContent(state)
    LaunchedEffect(viewModel) {
        viewModel.load()
    }
}

@Composable
private fun SmsScreenContent(
    state: SmsScreenState
) {
    Column {
        IvyToolbar(onBack = {}) {
            Text("Транзакции из смс")
        }
        LazyColumn {
            items(items = state.items, key = { item -> item.id }) { item: SmsModel ->
                SmsTransactionItem(
                    smsModel = item,
                    baseCurrency = state.baseCurrency,
                    onClick = {
                        // TODO: open screen where we choose account with other editable transaction fields
                    }
                )
            }
        }
    }
}

@Composable
private fun SmsTransactionItem(
    smsModel: SmsModel,
    baseCurrency: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
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
        SmsTransactionItem(
            smsModel = SmsModel(
                id = "1234",
                cardLastDigits = "3456",
                date = Clock.System.now(),
                amount = 123.45,
                consumer = "ATM PBT"
            ),
            baseCurrency = "USD",
            onClick = {}
        )
    }
}
