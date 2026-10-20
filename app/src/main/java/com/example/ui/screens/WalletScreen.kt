package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BankAccountEntity
import com.example.data.model.WalletEntity
import com.example.ui.theme.*

val topupAmounts = listOf("200", "500", "1000", "2000", "5000")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    wallet: WalletEntity?,
    bankAccounts: List<BankAccountEntity>,
    onTopUp: (amount: Double, bankId: Long) -> Unit,
    onTransferToBank: (amount: Double, bankId: Long) -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    var topUpAmount by remember { mutableStateOf("500") }
    var transferAmount by remember { mutableStateOf("500") }
    var activeTab by remember { mutableStateOf(0) } // 0: Add Money, 1: Transfer to Bank

    val primaryBank = bankAccounts.find { it.isPrimary } ?: bankAccounts.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xpay Wallet & UPI Lite", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .testTag("wallet_screen")
        ) {
            // Hero Wallet Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(CardGradient2Start, CardGradient2End)
                        )
                    )
                    .border(1.dp, NeonPurpleLight.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(22.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = NeonPurpleLight,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Xpay Digital Wallet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // UPI Lite Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MintGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "⚡ UPI Lite",
                                style = MaterialTheme.typography.labelSmall,
                                color = MintGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Total Available Balance",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "₹${String.format("%,.2f", wallet?.balance ?: 2450.00)}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Zero PIN required for instant payments under ₹500. 100% bank success rate.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tab Selector: Add Money vs Transfer to Bank
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                indicator = {},
                divider = {},
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Text(
                            "Add Money",
                            fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTab == 0) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent)
                )

                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Text(
                            "Transfer to Bank",
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTab == 1) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (activeTab == 0) {
                // Add Money Section
                Text(
                    text = "Amount to Add",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = topUpAmount,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) topUpAmount = it },
                    leadingIcon = { Text("₹", style = MaterialTheme.typography.titleLarge, color = ElectricCyan) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("wallet_topup_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(topupAmounts) { amt ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (topUpAmount == amt) ElectricCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (topUpAmount == amt) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricCyan)) else CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.clickable { topUpAmount = amt }
                        ) {
                            Text(
                                text = "+ ₹$amt",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                color = if (topUpAmount == amt) ElectricCyan else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Source Bank info
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = ElectricCyan)
                            Column {
                                Text(
                                    text = "Pay using ${primaryBank?.bankName ?: "HDFC Bank"}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${primaryBank?.accountNumber ?: "•••• 4829"} • Bal: ₹${primaryBank?.balance?.toInt() ?: 42850}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val amt = topUpAmount.toDoubleOrNull()
                        val bankId = primaryBank?.id ?: 1
                        if (amt != null && amt > 0) {
                            onTopUp(amt, bankId)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_wallet_topup"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Add ₹$topUpAmount to Wallet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                // Transfer to Bank Section
                Text(
                    text = "Transfer to Primary Bank Account",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = transferAmount,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) transferAmount = it },
                    leadingIcon = { Text("₹", style = MaterialTheme.typography.titleLarge, color = ElectricCyan) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MintGreen)
                        Column {
                            Text(
                                text = "Receiving Bank: ${primaryBank?.bankName ?: "HDFC Bank"}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Account: ${primaryBank?.accountNumber ?: "•••• 4829"} • IFSC: ${primaryBank?.ifscCode ?: "HDFC0001234"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val amt = transferAmount.toDoubleOrNull()
                        val bankId = primaryBank?.id ?: 1
                        if (amt != null && amt > 0) {
                            onTransferToBank(amt, bankId)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_wallet_transfer"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Transfer ₹$transferAmount to Bank",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
