package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 1,
    val fullName: String,
    val mobile: String,
    val email: String,
    val upiId: String,
    val upiPin: String = "1234",
    val biometricEnabled: Boolean = true,
    val dailyLimit: Double = 100000.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bank_accounts")
data class BankAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val bankName: String,
    val accountNumber: String,
    val ifscCode: String,
    val balance: Double,
    val isPrimary: Boolean = false,
    val accountType: String = "Savings",
    val cardColorIndex: Int = 0 // 0: Indigo Navy, 1: Deep Violet, 2: Emerald Green, 3: Royal Blue
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val senderId: Long = 1,
    val senderName: String,
    val senderUpiId: String,
    val receiverName: String,
    val receiverUpiId: String,
    val amount: Double,
    val transactionType: String, // "UPI_TRANSFER", "SCAN_PAY", "WALLET_TOPUP", "WALLET_TRANSFER", "BILL_PAYMENT", "CASHBACK"
    val transactionStatus: String = "SUCCESS", // "SUCCESS", "PENDING", "FAILED"
    val transactionDate: Long = System.currentTimeMillis(),
    val note: String = "",
    val utrNumber: String,
    val category: String = "Transfer", // "Food", "Shopping", "Bills", "Travel", "Grocery", "Transfer", "Rewards"
    val isDebit: Boolean = true
)

@Entity(tableName = "wallet")
data class WalletEntity(
    @PrimaryKey
    val userId: Long = 1,
    val balance: Double = 2500.0,
    val isUpiLiteEnabled: Boolean = true
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val title: String,
    val message: String,
    val type: String, // "PAYMENT_SENT", "PAYMENT_RECEIVED", "CASHBACK", "SECURITY", "ALERT"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mobile: String,
    val upiId: String,
    val avatarColorHex: Long = 0xFF7C4DFF,
    val isFavorite: Boolean = false
)
