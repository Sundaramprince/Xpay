package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.XpayDatabase
import com.example.data.model.BankAccountEntity
import com.example.data.model.ContactEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.WalletEntity
import com.example.data.repository.XpayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    SEND_MONEY,
    RECEIVE_MONEY,
    SCAN_QR,
    WALLET,
    BANK_ACCOUNTS,
    TRANSACTIONS,
    ANALYTICS,
    REWARDS,
    NOTIFICATIONS,
    PROFILE,
    LOGIN
}

enum class PinPurpose {
    PAYMENT,
    CHECK_BALANCE,
    CHANGE_PIN
}

data class PaymentDraft(
    val receiverName: String,
    val receiverUpiId: String,
    val amount: Double,
    val note: String,
    val category: String,
    val useWallet: Boolean,
    val bankAccountId: Long?
)

data class RewardCard(
    val id: Int,
    val title: String,
    val description: String,
    val cashbackAmount: Double,
    var isScratched: Boolean = false,
    val bannerColor: Long = 0xFFF59E0B
)

class XpayViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: XpayRepository

    init {
        val database = XpayDatabase.getDatabase(application, viewModelScope)
        repository = XpayRepository(database.xpayDao())
    }

    // App Navigation State
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _navigationStack = mutableListOf<Screen>()

    // Theme state
    val isDarkTheme = MutableStateFlow(true)

    // Data Flows
    val user: StateFlow<UserEntity?> = repository.user.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val bankAccounts: StateFlow<List<BankAccountEntity>> = repository.bankAccounts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val wallet: StateFlow<WalletEntity?> = repository.wallet.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val transactions: StateFlow<List<TransactionEntity>> = repository.transactions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    val contacts: StateFlow<List<ContactEntity>> = repository.contacts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // UPI PIN BottomSheet & Flow
    val isPinSheetVisible = MutableStateFlow(false)
    val pinPurpose = MutableStateFlow(PinPurpose.PAYMENT)
    val enteredPin = MutableStateFlow("")
    val pinError = MutableStateFlow<String?>(null)
    private var pendingPayment: PaymentDraft? = null
    val balanceCheckAccount = MutableStateFlow<BankAccountEntity?>(null)
    val revealedBalance = MutableStateFlow<Double?>(null)

    // Send Money Form State
    val sendReceiverName = MutableStateFlow("")
    val sendReceiverUpiId = MutableStateFlow("")
    val sendAmount = MutableStateFlow("")
    val sendNote = MutableStateFlow("")
    val sendCategory = MutableStateFlow("Transfer")
    val sendUseWallet = MutableStateFlow(false)
    val sendSelectedBankId = MutableStateFlow<Long?>(null)

    // Transaction Receipt
    val latestReceipt = MutableStateFlow<TransactionEntity?>(null)

    // Add Bank Account Form State
    val isAddBankSheetVisible = MutableStateFlow(false)

    // Scratch Cards & Rewards
    val rewardCards = MutableStateFlow(
        listOf(
            RewardCard(1, "Golden Weekend Bonus", "Scratch to unlock surprise UPI cashback", 35.0, false, 0xFFF59E0B),
            RewardCard(2, "Cred Club Special", "Cashback for maintaining flawless payment streak", 50.0, false, 0xFF8B5CF6),
            RewardCard(3, "Kirana Merchant Perk", "Instant reward for local QR payment", 20.0, false, 0xFF10B981),
            RewardCard(4, "Power User Bonanza", "Milestone reward on ₹10k+ monthly spends", 75.0, false, 0xFFEC4899)
        )
    )

    // UI Feedback
    val snackbarMessage = MutableStateFlow<String?>(null)

    // Transactions Screen Filters
    val transactionFilter = MutableStateFlow("ALL")
    val transactionSearchQuery = MutableStateFlow("")

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            _navigationStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (_navigationStack.isNotEmpty()) {
            _currentScreen.value = _navigationStack.removeAt(_navigationStack.size - 1)
            return true
        }
        if (_currentScreen.value != Screen.HOME) {
            _currentScreen.value = Screen.HOME
            return true
        }
        return false
    }

    fun prepareSendMoney(name: String, upiId: String, suggestedAmount: String = "", category: String = "Transfer") {
        sendReceiverName.value = name
        sendReceiverUpiId.value = upiId
        if (suggestedAmount.isNotBlank()) {
            sendAmount.value = suggestedAmount
        }
        sendCategory.value = category
        navigateTo(Screen.SEND_MONEY)
    }

    fun initiatePayment() {
        val amount = sendAmount.value.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            snackbarMessage.value = "Please enter a valid amount"
            return
        }
        if (sendReceiverUpiId.value.isBlank()) {
            snackbarMessage.value = "Please specify recipient UPI ID or Phone"
            return
        }

        pendingPayment = PaymentDraft(
            receiverName = sendReceiverName.value.ifBlank { sendReceiverUpiId.value },
            receiverUpiId = sendReceiverUpiId.value,
            amount = amount,
            note = sendNote.value,
            category = sendCategory.value,
            useWallet = sendUseWallet.value,
            bankAccountId = sendSelectedBankId.value
        )

        pinPurpose.value = PinPurpose.PAYMENT
        enteredPin.value = ""
        pinError.value = null
        isPinSheetVisible.value = true
    }

    fun initiateCheckBalance(account: BankAccountEntity) {
        balanceCheckAccount.value = account
        revealedBalance.value = null
        pinPurpose.value = PinPurpose.CHECK_BALANCE
        enteredPin.value = ""
        pinError.value = null
        isPinSheetVisible.value = true
    }

    fun onPinDigit(digit: String) {
        if (enteredPin.value.length < 4) {
            enteredPin.value += digit
            if (enteredPin.value.length == 4) {
                submitPin(enteredPin.value)
            }
        }
    }

    fun onPinBackspace() {
        if (enteredPin.value.isNotEmpty()) {
            enteredPin.value = enteredPin.value.dropLast(1)
            pinError.value = null
        }
    }

    fun onBiometricAuthSuccess() {
        // Biometric shortcut automatically uses user's stored PIN
        viewModelScope.launch {
            val u = user.value
            if (u != null) {
                submitPin(u.upiPin)
            }
        }
    }

    private fun submitPin(pin: String) {
        viewModelScope.launch {
            when (pinPurpose.value) {
                PinPurpose.PAYMENT -> {
                    val draft = pendingPayment ?: return@launch
                    val result = repository.sendPayment(
                        receiverName = draft.receiverName,
                        receiverUpiId = draft.receiverUpiId,
                        amount = draft.amount,
                        note = draft.note,
                        category = draft.category,
                        useWallet = draft.useWallet,
                        bankAccountId = draft.bankAccountId,
                        enteredPin = pin
                    )
                    if (result.isSuccess) {
                        isPinSheetVisible.value = false
                        enteredPin.value = ""
                        latestReceipt.value = result.getOrNull()
                        // Reset send fields
                        sendAmount.value = ""
                        sendNote.value = ""
                        pendingPayment = null
                        snackbarMessage.value = "Payment Successful!"
                    } else {
                        pinError.value = result.exceptionOrNull()?.message ?: "Transaction Failed"
                        enteredPin.value = ""
                    }
                }
                PinPurpose.CHECK_BALANCE -> {
                    val isValid = repository.verifyUpiPin(pin)
                    if (isValid) {
                        isPinSheetVisible.value = false
                        revealedBalance.value = balanceCheckAccount.value?.balance
                        enteredPin.value = ""
                    } else {
                        pinError.value = "Incorrect UPI PIN"
                        enteredPin.value = ""
                    }
                }
                PinPurpose.CHANGE_PIN -> {
                    val res = repository.updateUpiPin(pin)
                    if (res.isSuccess) {
                        isPinSheetVisible.value = false
                        enteredPin.value = ""
                        snackbarMessage.value = "UPI PIN updated successfully!"
                    } else {
                        pinError.value = res.exceptionOrNull()?.message
                        enteredPin.value = ""
                    }
                }
            }
        }
    }

    fun dismissPinSheet() {
        isPinSheetVisible.value = false
        enteredPin.value = ""
        pinError.value = null
    }

    fun dismissReceipt() {
        latestReceipt.value = null
        navigateTo(Screen.HOME)
    }

    fun topUpWallet(amount: Double, bankAccountId: Long) {
        viewModelScope.launch {
            val res = repository.topUpWallet(amount, bankAccountId)
            if (res.isSuccess) {
                snackbarMessage.value = "Added ₹${amount.toInt()} to Xpay Wallet"
            } else {
                snackbarMessage.value = res.exceptionOrNull()?.message ?: "Topup failed"
            }
        }
    }

    fun transferWalletToBank(amount: Double, bankAccountId: Long) {
        viewModelScope.launch {
            val res = repository.transferWalletToBank(amount, bankAccountId)
            if (res.isSuccess) {
                snackbarMessage.value = "Transferred ₹${amount.toInt()} to Bank Account"
            } else {
                snackbarMessage.value = res.exceptionOrNull()?.message ?: "Transfer failed"
            }
        }
    }

    fun addBankAccount(bankName: String, accountNumber: String, ifsc: String, initialBalance: Double) {
        viewModelScope.launch {
            repository.addBankAccount(bankName, accountNumber, ifsc, initialBalance)
            isAddBankSheetVisible.value = false
            snackbarMessage.value = "$bankName linked successfully"
        }
    }

    fun setPrimaryBank(id: Long) {
        viewModelScope.launch {
            repository.setPrimaryBank(id)
            snackbarMessage.value = "Primary account updated"
        }
    }

    fun removeBankAccount(id: Long) {
        viewModelScope.launch {
            repository.removeBankAccount(id)
            snackbarMessage.value = "Bank account unlinked"
        }
    }

    fun scratchReward(cardId: Int) {
        val currentList = rewardCards.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == cardId }
        if (index != -1 && !currentList[index].isScratched) {
            val card = currentList[index]
            currentList[index] = card.copy(isScratched = true)
            rewardCards.value = currentList
            viewModelScope.launch {
                repository.awardCashback(card.cashbackAmount, "Won ${card.title}")
                snackbarMessage.value = "🎉 ₹${card.cashbackAmount.toInt()} Cashback credited to wallet!"
            }
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch { repository.markNotificationAsRead(id) }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch { repository.markAllNotificationsAsRead() }
    }

    fun clearAllNotifications() {
        viewModelScope.launch { repository.clearAllNotifications() }
    }

    fun updateProfile(name: String, email: String, upiId: String, limit: Double) {
        viewModelScope.launch {
            repository.updateUserProfile(name, email, upiId, limit)
            snackbarMessage.value = "Profile updated"
        }
    }

    fun toggleBiometrics(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleBiometrics(enabled)
            snackbarMessage.value = if (enabled) "Biometric Lock Enabled" else "Biometric Lock Disabled"
        }
    }

    fun clearSnackbar() {
        snackbarMessage.value = null
    }
}
