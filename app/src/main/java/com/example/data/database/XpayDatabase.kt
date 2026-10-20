package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.XpayDao
import com.example.data.model.BankAccountEntity
import com.example.data.model.ContactEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.WalletEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        BankAccountEntity::class,
        TransactionEntity::class,
        WalletEntity::class,
        NotificationEntity::class,
        ContactEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class XpayDatabase : RoomDatabase() {
    abstract fun xpayDao(): XpayDao

    companion object {
        @Volatile
        private var INSTANCE: XpayDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): XpayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    XpayDatabase::class.java,
                    "xpay_database"
                )
                    .addCallback(XpayDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class XpayDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.xpayDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: XpayDao) {
            // Seed User
            val defaultUser = UserEntity(
                id = 1,
                fullName = "Alex Johnson",
                mobile = "9876543210",
                email = "alex.johnson@xpay.com",
                upiId = "alex@xpay",
                upiPin = "1234",
                biometricEnabled = true,
                dailyLimit = 100000.0
            )
            dao.insertUser(defaultUser)

            // Seed Bank Accounts
            val bank1 = BankAccountEntity(
                id = 1,
                userId = 1,
                bankName = "HDFC Bank",
                accountNumber = "•••• 4829",
                ifscCode = "HDFC0001234",
                balance = 42850.50,
                isPrimary = true,
                accountType = "Savings",
                cardColorIndex = 0
            )
            val bank2 = BankAccountEntity(
                id = 2,
                userId = 1,
                bankName = "State Bank of India",
                accountNumber = "•••• 9012",
                ifscCode = "SBIN0005432",
                balance = 18320.00,
                isPrimary = false,
                accountType = "Savings",
                cardColorIndex = 1
            )
            val bank3 = BankAccountEntity(
                id = 3,
                userId = 1,
                bankName = "ICICI Bank",
                accountNumber = "•••• 7741",
                ifscCode = "ICIC0003112",
                balance = 65400.00,
                isPrimary = false,
                accountType = "Current",
                cardColorIndex = 2
            )
            dao.insertBankAccount(bank1)
            dao.insertBankAccount(bank2)
            dao.insertBankAccount(bank3)

            // Seed Wallet
            val defaultWallet = WalletEntity(
                userId = 1,
                balance = 2450.00,
                isUpiLiteEnabled = true
            )
            dao.insertWallet(defaultWallet)

            // Seed Transactions
            val now = System.currentTimeMillis()
            val day = 86400000L
            val transactions = listOf(
                TransactionEntity(
                    id = "TXN9812401",
                    senderId = 1,
                    senderName = "Alex Johnson",
                    senderUpiId = "alex@xpay",
                    receiverName = "Starbucks Coffee",
                    receiverUpiId = "starbucks@icici",
                    amount = 350.00,
                    transactionType = "SCAN_PAY",
                    transactionStatus = "SUCCESS",
                    transactionDate = now - 3600000L * 2,
                    note = "Caramel Macchiato & Cookie",
                    utrNumber = "428901847192",
                    category = "Food",
                    isDebit = true
                ),
                TransactionEntity(
                    id = "TXN9812402",
                    senderId = 2,
                    senderName = "Priya Sharma",
                    senderUpiId = "priya.s@okhdfcbank",
                    receiverName = "Alex Johnson",
                    receiverUpiId = "alex@xpay",
                    amount = 1200.00,
                    transactionType = "UPI_TRANSFER",
                    transactionStatus = "SUCCESS",
                    transactionDate = now - 3600000L * 8,
                    note = "Dinner split share",
                    utrNumber = "428901847193",
                    category = "Transfer",
                    isDebit = false
                ),
                TransactionEntity(
                    id = "TXN9812403",
                    senderId = 1,
                    senderName = "Alex Johnson",
                    senderUpiId = "alex@xpay",
                    receiverName = "Airtel Broadband",
                    receiverUpiId = "airtelbill@paytm",
                    amount = 899.00,
                    transactionType = "BILL_PAYMENT",
                    transactionStatus = "SUCCESS",
                    transactionDate = now - day,
                    note = "Monthly Fiber Plan",
                    utrNumber = "428901847194",
                    category = "Bills",
                    isDebit = true
                ),
                TransactionEntity(
                    id = "TXN9812404",
                    senderId = 0,
                    senderName = "Xpay Rewards",
                    senderUpiId = "rewards@xpay",
                    receiverName = "Alex Johnson",
                    receiverUpiId = "alex@xpay",
                    amount = 50.00,
                    transactionType = "CASHBACK",
                    transactionStatus = "SUCCESS",
                    transactionDate = now - day * 2,
                    note = "Gold Scratch Card Cashback",
                    utrNumber = "428901847195",
                    category = "Rewards",
                    isDebit = false
                ),
                TransactionEntity(
                    id = "TXN9812405",
                    senderId = 1,
                    senderName = "Alex Johnson",
                    senderUpiId = "alex@xpay",
                    receiverName = "Swiggy Gourmet",
                    receiverUpiId = "swiggy@axisbank",
                    amount = 540.00,
                    transactionType = "UPI_TRANSFER",
                    transactionStatus = "SUCCESS",
                    transactionDate = now - day * 3,
                    note = "Weekend Bento Box",
                    utrNumber = "428901847196",
                    category = "Food",
                    isDebit = true
                ),
                TransactionEntity(
                    id = "TXN9812406",
                    senderId = 1,
                    senderName = "Alex Johnson",
                    senderUpiId = "alex@xpay",
                    receiverName = "Uber Technologies",
                    receiverUpiId = "uber@hdfcbank",
                    amount = 310.00,
                    transactionType = "SCAN_PAY",
                    transactionStatus = "SUCCESS",
                    transactionDate = now - day * 4,
                    note = "Airport ride",
                    utrNumber = "428901847197",
                    category = "Travel",
                    isDebit = true
                )
            )
            transactions.forEach { dao.insertTransaction(it) }

            // Seed Contacts
            val contacts = listOf(
                ContactEntity(1, "Priya Sharma", "9811223344", "priya.s@okhdfcbank", 0xFFE91E63, true),
                ContactEntity(2, "Rohan Verma", "9822334455", "rohan.v@axisbank", 0xFF00E5FF, true),
                ContactEntity(3, "Sneha Patel", "9833445566", "sneha.p@paytm", 0xFF7C4DFF, true),
                ContactEntity(4, "Vikram Singh", "9844556677", "vikram@sbi", 0xFF00E676, false),
                ContactEntity(5, "Ananya Roy", "9855667788", "ananya@icici", 0xFFFF9100, true),
                ContactEntity(6, "Deepak Nair", "9866778899", "deepak@kotak", 0xFF00B0FF, false)
            )
            dao.insertContacts(contacts)

            // Seed Notifications
            val notifications = listOf(
                NotificationEntity(
                    id = 1,
                    userId = 1,
                    title = "Welcome to Xpay Premium!",
                    message = "Enjoy seamless UPI payments, 0% wallet load charges & instant cashbacks.",
                    type = "SECURITY",
                    timestamp = now - day * 5,
                    isRead = true
                ),
                NotificationEntity(
                    id = 2,
                    userId = 1,
                    title = "Cashback Won: ₹50",
                    message = "You won ₹50 gold cashback for your utility bill payment! Added to wallet.",
                    type = "CASHBACK",
                    timestamp = now - day * 2,
                    isRead = false
                ),
                NotificationEntity(
                    id = 3,
                    userId = 1,
                    title = "Money Received from Priya Sharma",
                    message = "₹1,200 credited to HDFC Bank A/C ••4829 via UPI Ref: 428901847193.",
                    type = "PAYMENT_RECEIVED",
                    timestamp = now - 3600000L * 8,
                    isRead = false
                )
            )
            notifications.forEach { dao.insertNotification(it) }
        }
    }
}
