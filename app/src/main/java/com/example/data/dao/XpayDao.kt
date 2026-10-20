package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BankAccountEntity
import com.example.data.model.ContactEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface XpayDao {
    // User
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUser(userId: Long = 1): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserSync(userId: Long = 1): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    // Bank Accounts
    @Query("SELECT * FROM bank_accounts WHERE userId = :userId ORDER BY isPrimary DESC, id ASC")
    fun getBankAccounts(userId: Long = 1): Flow<List<BankAccountEntity>>

    @Query("SELECT * FROM bank_accounts WHERE userId = :userId AND isPrimary = 1 LIMIT 1")
    suspend fun getPrimaryBankAccount(userId: Long = 1): BankAccountEntity?

    @Query("SELECT * FROM bank_accounts WHERE id = :id LIMIT 1")
    suspend fun getBankAccountById(id: Long): BankAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankAccount(account: BankAccountEntity)

    @Update
    suspend fun updateBankAccount(account: BankAccountEntity)

    @Query("UPDATE bank_accounts SET isPrimary = CASE WHEN id = :selectedId THEN 1 ELSE 0 END WHERE userId = :userId")
    suspend fun setPrimaryBankAccount(userId: Long, selectedId: Long)

    @Query("DELETE FROM bank_accounts WHERE id = :id")
    suspend fun deleteBankAccount(id: Long)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY transactionDate DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    // Wallet
    @Query("SELECT * FROM wallet WHERE userId = :userId LIMIT 1")
    fun getWallet(userId: Long = 1): Flow<WalletEntity?>

    @Query("SELECT * FROM wallet WHERE userId = :userId LIMIT 1")
    suspend fun getWalletSync(userId: Long = 1): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadNotificationsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    // Contacts
    @Query("SELECT * FROM contacts ORDER BY isFavorite DESC, name ASC")
    fun getContacts(): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)
}
