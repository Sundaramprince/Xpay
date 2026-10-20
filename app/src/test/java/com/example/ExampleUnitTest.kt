package com.example

import com.example.data.model.BankAccountEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.WalletEntity
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testUserEntityCreation() {
        val user = UserEntity(
            id = 1,
            fullName = "Alex Johnson",
            mobile = "9876543210",
            email = "alex@xpay.com",
            upiId = "alex@xpay",
            upiPin = "1234"
        )
        assertEquals("Alex Johnson", user.fullName)
        assertEquals("alex@xpay", user.upiId)
        assertEquals("1234", user.upiPin)
        assertTrue(user.biometricEnabled)
        assertEquals(100000.0, user.dailyLimit, 0.01)
    }

    @Test
    fun testTransactionCalculation() {
        val initialBalance = 10000.0
        val sendAmount = 1500.0
        val updatedBalance = initialBalance - sendAmount
        assertEquals(8500.0, updatedBalance, 0.001)

        val txn = TransactionEntity(
            id = "TXN12345678",
            senderName = "Alex",
            senderUpiId = "alex@xpay",
            receiverName = "Priya",
            receiverUpiId = "priya@upi",
            amount = sendAmount,
            transactionType = "UPI_TRANSFER",
            utrNumber = "123456789012",
            isDebit = true
        )
        assertTrue(txn.isDebit)
        assertEquals(12, txn.utrNumber.length)
        assertEquals("SUCCESS", txn.transactionStatus)
    }

    @Test
    fun testWalletBalanceTransfer() {
        val wallet = WalletEntity(userId = 1, balance = 2500.0)
        val topup = 500.0
        val newWallet = wallet.copy(balance = wallet.balance + topup)
        assertEquals(3000.0, newWallet.balance, 0.001)

        val bank = BankAccountEntity(
            id = 1,
            bankName = "HDFC Bank",
            accountNumber = "•••• 4829",
            ifscCode = "HDFC0001234",
            balance = 40000.0
        )
        val newBank = bank.copy(balance = bank.balance - topup)
        assertEquals(39500.0, newBank.balance, 0.001)
    }
}
