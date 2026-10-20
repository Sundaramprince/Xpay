package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddBankAccountBottomSheet
import com.example.ui.components.TransactionReceiptDialog
import com.example.ui.components.UpiPinBottomSheet
import com.example.ui.components.XpayBottomNav
import com.example.ui.components.XpayTopBar
import com.example.ui.screens.*
import com.example.ui.theme.XpayTheme
import com.example.ui.viewmodel.PinPurpose
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.XpayViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: XpayViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

            XpayTheme(darkTheme = isDarkTheme) {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val user by viewModel.user.collectAsStateWithLifecycle()
                val bankAccounts by viewModel.bankAccounts.collectAsStateWithLifecycle()
                val wallet by viewModel.wallet.collectAsStateWithLifecycle()
                val transactions by viewModel.transactions.collectAsStateWithLifecycle()
                val notifications by viewModel.notifications.collectAsStateWithLifecycle()
                val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
                val contacts by viewModel.contacts.collectAsStateWithLifecycle()
                val revealedBalance by viewModel.revealedBalance.collectAsStateWithLifecycle()

                // Bottom sheets & dialogs state
                val isPinSheetVisible by viewModel.isPinSheetVisible.collectAsStateWithLifecycle()
                val pinPurpose by viewModel.pinPurpose.collectAsStateWithLifecycle()
                val enteredPin by viewModel.enteredPin.collectAsStateWithLifecycle()
                val pinError by viewModel.pinError.collectAsStateWithLifecycle()
                val latestReceipt by viewModel.latestReceipt.collectAsStateWithLifecycle()
                val isAddBankSheetVisible by viewModel.isAddBankSheetVisible.collectAsStateWithLifecycle()
                val rewardCards by viewModel.rewardCards.collectAsStateWithLifecycle()
                val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

                val primaryBank = bankAccounts.find { it.isPrimary } ?: bankAccounts.firstOrNull()

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(snackbarMessage) {
                    snackbarMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearSnackbar()
                    }
                }

                val showBottomNav = currentScreen in listOf(
                    Screen.HOME,
                    Screen.TRANSACTIONS,
                    Screen.WALLET,
                    Screen.PROFILE
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (currentScreen == Screen.HOME) {
                            XpayTopBar(
                                userName = user?.fullName ?: "Alex Johnson",
                                unreadNotifications = unreadNotificationsCount,
                                onAvatarClick = { viewModel.navigateTo(Screen.PROFILE) },
                                onScanClick = { viewModel.navigateTo(Screen.SCAN_QR) },
                                onRewardsClick = { viewModel.navigateTo(Screen.REWARDS) },
                                onNotificationsClick = { viewModel.navigateTo(Screen.NOTIFICATIONS) }
                            )
                        }
                    },
                    bottomBar = {
                        if (showBottomNav) {
                            XpayBottomNav(
                                currentScreen = currentScreen,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            Screen.HOME -> {
                                HomeScreen(
                                    user = user,
                                    primaryBank = primaryBank,
                                    allBanks = bankAccounts,
                                    wallet = wallet,
                                    contacts = contacts,
                                    recentTransactions = transactions,
                                    revealedBalance = revealedBalance,
                                    onCheckBalance = { viewModel.initiateCheckBalance(it) },
                                    onNavigate = { viewModel.navigateTo(it) },
                                    onQuickSendToContact = {
                                        viewModel.prepareSendMoney(it.name, it.upiId)
                                    },
                                    onTransactionClick = {
                                        viewModel.latestReceipt.value = it
                                    }
                                )
                            }
                            Screen.SEND_MONEY -> {
                                val sendRecipientName by viewModel.sendReceiverName.collectAsStateWithLifecycle()
                                val sendRecipientUpiId by viewModel.sendReceiverUpiId.collectAsStateWithLifecycle()
                                val sendAmount by viewModel.sendAmount.collectAsStateWithLifecycle()
                                val sendNote by viewModel.sendNote.collectAsStateWithLifecycle()
                                val sendCategory by viewModel.sendCategory.collectAsStateWithLifecycle()
                                val sendUseWallet by viewModel.sendUseWallet.collectAsStateWithLifecycle()
                                val sendSelectedBankId by viewModel.sendSelectedBankId.collectAsStateWithLifecycle()

                                SendMoneyScreen(
                                    recipientName = sendRecipientName,
                                    recipientUpiId = sendRecipientUpiId,
                                    amount = sendAmount,
                                    note = sendNote,
                                    category = sendCategory,
                                    useWallet = sendUseWallet,
                                    selectedBankId = sendSelectedBankId,
                                    bankAccounts = bankAccounts,
                                    wallet = wallet,
                                    contacts = contacts,
                                    onNameChange = { viewModel.sendReceiverName.value = it },
                                    onUpiIdChange = { viewModel.sendReceiverUpiId.value = it },
                                    onAmountChange = { viewModel.sendAmount.value = it },
                                    onNoteChange = { viewModel.sendNote.value = it },
                                    onCategoryChange = { viewModel.sendCategory.value = it },
                                    onUseWalletChange = { viewModel.sendUseWallet.value = it },
                                    onBankIdChange = { viewModel.sendSelectedBankId.value = it },
                                    onPayClick = { viewModel.initiatePayment() },
                                    onBackClick = { viewModel.navigateBack() }
                                )
                            }
                            Screen.RECEIVE_MONEY -> {
                                ReceiveMoneyScreen(
                                    user = user,
                                    onBackClick = { viewModel.navigateBack() }
                                )
                            }
                            Screen.SCAN_QR -> {
                                ScanQrScreen(
                                    onQrScanned = { name, upiId, amount, category ->
                                        viewModel.prepareSendMoney(name, upiId, amount, category)
                                    },
                                    onBackClick = { viewModel.navigateBack() }
                                )
                            }
                            Screen.WALLET -> {
                                WalletScreen(
                                    wallet = wallet,
                                    bankAccounts = bankAccounts,
                                    onTopUp = { amt, bankId -> viewModel.topUpWallet(amt, bankId) },
                                    onTransferToBank = { amt, bankId -> viewModel.transferWalletToBank(amt, bankId) },
                                    onBackClick = { viewModel.navigateBack() }
                                )
                            }
                            Screen.BANK_ACCOUNTS -> {
                                BankAccountsScreen(
                                    bankAccounts = bankAccounts,
                                    onCheckBalance = { viewModel.initiateCheckBalance(it) },
                                    onSetPrimary = { viewModel.setPrimaryBank(it) },
                                    onRemoveBank = { viewModel.removeBankAccount(it) },
                                    onAddBankClick = { viewModel.isAddBankSheetVisible.value = true },
                                    onBackClick = { viewModel.navigateBack() }
                                )
                            }
                            Screen.TRANSACTIONS -> {
                                TransactionsScreen(
                                    transactions = transactions,
                                    onTransactionClick = { viewModel.latestReceipt.value = it },
                                    onBackClick = { viewModel.navigateBack() }
                                )
                            }
                            Screen.ANALYTICS -> {
                                AnalyticsScreen(
                                    transactions = transactions,
                                    onBackClick = { viewModel.navigateBack() }
                                )
                            }
                            Screen.REWARDS -> {
                                RewardsScreen(
                                    rewardCards = rewardCards,
                                    onScratchCard = { viewModel.scratchReward(it) },
                                    onBackClick = { viewModel.navigateBack() }
                                )
                            }
                            Screen.NOTIFICATIONS -> {
                                NotificationsScreen(
                                    notifications = notifications,
                                    onMarkAsRead = { viewModel.markNotificationRead(it) },
                                    onMarkAllRead = { viewModel.markAllNotificationsRead() },
                                    onClearAll = { viewModel.clearAllNotifications() },
                                    onBackClick = { viewModel.navigateBack() }
                                )
                            }
                            Screen.PROFILE -> {
                                ProfileScreen(
                                    user = user,
                                    isDarkTheme = isDarkTheme,
                                    onToggleDarkTheme = { viewModel.isDarkTheme.value = it },
                                    onToggleBiometrics = { viewModel.toggleBiometrics(it) },
                                    onChangeUpiPinClick = {
                                        viewModel.pinPurpose.value = PinPurpose.CHANGE_PIN
                                        viewModel.enteredPin.value = ""
                                        viewModel.pinError.value = null
                                        viewModel.isPinSheetVisible.value = true
                                    },
                                    onNavigate = { viewModel.navigateTo(it) },
                                    onBackClick = { viewModel.navigateBack() }
                                )
                            }
                            Screen.LOGIN -> {
                                // Default to home screen since initial user is seeded
                                viewModel.navigateTo(Screen.HOME)
                            }
                        }
                    }
                }

                // Modal Sheets and Dialogs
                UpiPinBottomSheet(
                    isVisible = isPinSheetVisible,
                    purpose = pinPurpose,
                    enteredPin = enteredPin,
                    errorMessage = pinError,
                    accountName = when (pinPurpose) {
                        PinPurpose.PAYMENT -> viewModel.sendReceiverName.value.ifBlank { viewModel.sendReceiverUpiId.value }
                        PinPurpose.CHECK_BALANCE -> viewModel.balanceCheckAccount.value?.bankName
                        PinPurpose.CHANGE_PIN -> "Xpay Security"
                    },
                    amount = viewModel.sendAmount.value.toDoubleOrNull(),
                    onDigitClick = { viewModel.onPinDigit(it) },
                    onBackspaceClick = { viewModel.onPinBackspace() },
                    onBiometricClick = { viewModel.onBiometricAuthSuccess() },
                    onDismiss = { viewModel.dismissPinSheet() }
                )

                TransactionReceiptDialog(
                    transaction = latestReceipt,
                    onDismiss = { viewModel.dismissReceipt() }
                )

                AddBankAccountBottomSheet(
                    isVisible = isAddBankSheetVisible,
                    onDismiss = { viewModel.isAddBankSheetVisible.value = false },
                    onAddBank = { name, acc, ifsc, bal ->
                        viewModel.addBankAccount(name, acc, ifsc, bal)
                    }
                )
            }
        }
    }
}
