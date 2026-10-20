package com.example.data.repository

import com.example.data.dao.XpayDao
import com.example.data.model.BankAccountEntity
import com.example.data.model.ContactEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.WalletEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.random.Random

class XpayRepository(private val dao: XpayDao) {

    val user: Flow<UserEntity?> = dao.getUser()
    val bankAccounts: Flow<List<BankAccountEntity>> = dao.getBankAccounts()
    val wallet: Flow<WalletEntity?> = dao.getWallet()
    val transactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val notifications: Flow<List<NotificationEntity>> = dao.getNotifications()
    val unreadNotificationsCount: Flow<Int> = dao.getUnreadNotificationsCount()
    val contacts: Flow<List<ContactEntity>> = dao.getContacts()

    suspend fun verifyUpiPin(pin: String): Boolean {
        val user = dao.getUserSync() ?: return false
        return user.upiPin == pin
    }

    suspend fun sendPayment(
        receiverName: String,
        receiverUpiId: String,
        amount: Double,
        note: String,
        category: String,
        useWallet: Boolean,
        bankAccountId: Long?,
        enteredPin: String
    ): Result<TransactionEntity> {
        val user = dao.getUserSync() ?: return Result.failure(Exception("User not found"))
        if (user.upiPin != enteredPin) {
            return Result.failure(Exception("Invalid UPI PIN. Please try again."))
        }

        if (amount <= 0) {
            return Result.failure(Exception("Amount must be greater than ₹0"))
        }

        if (amount > user.dailyLimit) {
            return Result.failure(Exception("Amount exceeds daily transaction limit of ₹${user.dailyLimit.toInt()}"))
        }

        val utr = generateUtr()
        val txnId = "TXN" + System.currentTimeMillis().toString().takeLast(8)

        if (useWallet) {
            val wallet = dao.getWalletSync() ?: return Result.failure(Exception("Wallet not initialized"))
            if (wallet.balance < amount) {
                return Result.failure(Exception("Insufficient wallet balance (Available: ₹${String.format("%.2f", wallet.balance)})"))
            }
            dao.updateWallet(wallet.copy(balance = wallet.balance - amount))
        } else {
            val bank = if (bankAccountId != null) {
                dao.getBankAccountById(bankAccountId)
            } else {
                dao.getPrimaryBankAccount()
            } ?: return Result.failure(Exception("No bank account selected"))

            if (bank.balance < amount) {
                return Result.failure(Exception("Insufficient balance in ${bank.bankName} (Available: ₹${String.format("%.2f", bank.balance)})"))
            }
            dao.updateBankAccount(bank.copy(balance = bank.balance - amount))
        }

        val transaction = TransactionEntity(
            id = txnId,
            senderId = user.id,
            senderName = user.fullName,
            senderUpiId = user.upiId,
            receiverName = receiverName,
            receiverUpiId = receiverUpiId,
            amount = amount,
            transactionType = if (category == "Bills") "BILL_PAYMENT" else "UPI_TRANSFER",
            transactionStatus = "SUCCESS",
            transactionDate = System.currentTimeMillis(),
            note = note.ifBlank { "Paid via Xpay" },
            utrNumber = utr,
            category = category,
            isDebit = true
        )

        dao.insertTransaction(transaction)

        // Add Notification
        dao.insertNotification(
            NotificationEntity(
                userId = user.id,
                title = "Payment Successful: ₹${String.format("%.2f", amount)}",
                message = "Sent to $receiverName ($receiverUpiId) via UTR: $utr",
                type = "PAYMENT_SENT",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )

        // Chance of instant cashback (25% chance for transfers > 100)
        if (amount >= 100 && Random.nextFloat() < 0.35f) {
            val cashbackAmount = Random.nextInt(10, 45).toDouble()
            awardCashback(cashbackAmount, "Cashback on $receiverName transfer")
        }

        return Result.success(transaction)
    }

    suspend fun topUpWallet(amount: Double, bankAccountId: Long): Result<Unit> {
        val user = dao.getUserSync() ?: return Result.failure(Exception("User not found"))
        val bank = dao.getBankAccountById(bankAccountId) ?: return Result.failure(Exception("Bank not found"))
        if (bank.balance < amount) {
            return Result.failure(Exception("Insufficient bank balance"))
        }

        val wallet = dao.getWalletSync() ?: WalletEntity(userId = user.id, balance = 0.0)
        dao.updateBankAccount(bank.copy(balance = bank.balance - amount))
        dao.updateWallet(wallet.copy(balance = wallet.balance + amount))

        val utr = generateUtr()
        dao.insertTransaction(
            TransactionEntity(
                id = "TXN" + System.currentTimeMillis().toString().takeLast(8),
                senderId = user.id,
                senderName = bank.bankName,
                senderUpiId = bank.accountNumber,
                receiverName = "Xpay Wallet",
                receiverUpiId = "wallet@xpay",
                amount = amount,
                transactionType = "WALLET_TOPUP",
                transactionStatus = "SUCCESS",
                transactionDate = System.currentTimeMillis(),
                note = "Added from ${bank.bankName}",
                utrNumber = utr,
                category = "Transfer",
                isDebit = false
            )
        )

        dao.insertNotification(
            NotificationEntity(
                userId = user.id,
                title = "Wallet Loaded: ₹${String.format("%.2f", amount)}",
                message = "Added from ${bank.bankName} to Xpay Wallet. Instant UPI Lite ready.",
                type = "ALERT",
                timestamp = System.currentTimeMillis()
            )
        )

        return Result.success(Unit)
    }

    suspend fun transferWalletToBank(amount: Double, bankAccountId: Long): Result<Unit> {
        val user = dao.getUserSync() ?: return Result.failure(Exception("User not found"))
        val wallet = dao.getWalletSync() ?: return Result.failure(Exception("Wallet not found"))
        if (wallet.balance < amount) {
            return Result.failure(Exception("Insufficient wallet balance"))
        }

        val bank = dao.getBankAccountById(bankAccountId) ?: return Result.failure(Exception("Bank not found"))
        dao.updateWallet(wallet.copy(balance = wallet.balance - amount))
        dao.updateBankAccount(bank.copy(balance = bank.balance + amount))

        val utr = generateUtr()
        dao.insertTransaction(
            TransactionEntity(
                id = "TXN" + System.currentTimeMillis().toString().takeLast(8),
                senderId = user.id,
                senderName = "Xpay Wallet",
                senderUpiId = "wallet@xpay",
                receiverName = bank.bankName,
                receiverUpiId = bank.accountNumber,
                amount = amount,
                transactionType = "WALLET_TRANSFER",
                transactionStatus = "SUCCESS",
                transactionDate = System.currentTimeMillis(),
                note = "Transferred to ${bank.bankName}",
                utrNumber = utr,
                category = "Transfer",
                isDebit = true
            )
        )

        dao.insertNotification(
            NotificationEntity(
                userId = user.id,
                title = "Transfer to Bank: ₹${String.format("%.2f", amount)}",
                message = "Transferred from wallet to ${bank.bankName} (${bank.accountNumber}).",
                type = "ALERT",
                timestamp = System.currentTimeMillis()
            )
        )

        return Result.success(Unit)
    }

    suspend fun awardCashback(amount: Double, reason: String) {
        val user = dao.getUserSync() ?: return
        val wallet = dao.getWalletSync() ?: WalletEntity(userId = user.id, balance = 0.0)
        dao.updateWallet(wallet.copy(balance = wallet.balance + amount))

        val utr = generateUtr()
        dao.insertTransaction(
            TransactionEntity(
                id = "TXN" + System.currentTimeMillis().toString().takeLast(8),
                senderId = 0,
                senderName = "Xpay Rewards",
                senderUpiId = "rewards@xpay",
                receiverName = user.fullName,
                receiverUpiId = user.upiId,
                amount = amount,
                transactionType = "CASHBACK",
                transactionStatus = "SUCCESS",
                transactionDate = System.currentTimeMillis(),
                note = reason,
                utrNumber = utr,
                category = "Rewards",
                isDebit = false
            )
        )

        dao.insertNotification(
            NotificationEntity(
                userId = user.id,
                title = "🎉 Cashback Credited: ₹${amount.toInt()}",
                message = "$reason. Transferred directly to your Xpay Wallet!",
                type = "CASHBACK",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun addBankAccount(
        bankName: String,
        accountNumber: String,
        ifscCode: String,
        initialBalance: Double
    ) {
        val user = dao.getUserSync() ?: return
        val maskedNumber = "•••• " + accountNumber.takeLast(4)
        val count = dao.getBankAccountById(0) // check
        val bank = BankAccountEntity(
            userId = user.id,
            bankName = bankName,
            accountNumber = maskedNumber,
            ifscCode = ifscCode.uppercase(),
            balance = initialBalance,
            isPrimary = false,
            cardColorIndex = Random.nextInt(0, 4)
        )
        dao.insertBankAccount(bank)

        dao.insertNotification(
            NotificationEntity(
                userId = user.id,
                title = "Bank Account Linked: $bankName",
                message = "Account ending in ${accountNumber.takeLast(4)} linked successfully to UPI.",
                type = "ALERT",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun setPrimaryBank(id: Long) {
        val user = dao.getUserSync() ?: return
        dao.setPrimaryBankAccount(user.id, id)
    }

    suspend fun removeBankAccount(id: Long) {
        dao.deleteBankAccount(id)
    }

    suspend fun updateUpiPin(newPin: String): Result<Unit> {
        val user = dao.getUserSync() ?: return Result.failure(Exception("User not found"))
        if (newPin.length != 4 && newPin.length != 6) {
            return Result.failure(Exception("UPI PIN must be 4 or 6 digits"))
        }
        dao.updateUser(user.copy(upiPin = newPin))
        dao.insertNotification(
            NotificationEntity(
                userId = user.id,
                title = "UPI PIN Changed",
                message = "Your Xpay UPI security PIN was updated successfully.",
                type = "SECURITY",
                timestamp = System.currentTimeMillis()
            )
        )
        return Result.success(Unit)
    }

    suspend fun updateUserProfile(name: String, email: String, upiId: String, limit: Double) {
        val user = dao.getUserSync() ?: return
        dao.updateUser(user.copy(fullName = name, email = email, upiId = upiId, dailyLimit = limit))
    }

    suspend fun toggleBiometrics(enabled: Boolean) {
        val user = dao.getUserSync() ?: return
        dao.updateUser(user.copy(biometricEnabled = enabled))
    }

    suspend fun markNotificationAsRead(id: Long) = dao.markNotificationAsRead(id)
    suspend fun markAllNotificationsAsRead() = dao.markAllNotificationsAsRead()
    suspend fun clearAllNotifications() = dao.clearAllNotifications()

    private fun generateUtr(): String {
        val sb = StringBuilder()
        repeat(12) {
            sb.append(Random.nextInt(0, 10))
        }
        return sb.toString()
    }
}
