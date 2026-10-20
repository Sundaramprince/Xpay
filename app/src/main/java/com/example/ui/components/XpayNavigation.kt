package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.NeonPurple
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XpayTopBar(
    userName: String,
    unreadNotifications: Int,
    onAvatarClick: () -> Unit,
    onScanClick: () -> Unit,
    onRewardsClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // User Avatar
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(ElectricCyan, NeonPurple))
                        )
                        .clickable { onAvatarClick() }
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Xpay",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ElectricCyan.copy(alpha = 0.15f))
                                .border(0.5.dp, ElectricCyan, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "UPI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan,
                                fontSize = 9.sp
                            )
                        }
                    }
                    Text(
                        text = "Instant & Zero Fee",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        },
        actions = {
            // Rewards Icon
            IconButton(
                onClick = onRewardsClick,
                modifier = Modifier.testTag("top_bar_rewards_button")
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Rewards",
                    tint = GoldYellow
                )
            }

            // Quick Scanner Icon
            IconButton(
                onClick = onScanClick,
                modifier = Modifier.testTag("top_bar_scan_button")
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan Any QR",
                    tint = ElectricCyan
                )
            }

            // Notifications Bell with Badge
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.testTag("top_bar_notifications_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotifications > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            ) {
                                Text("$unreadNotifications")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun XpayBottomNav(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("xpay_bottom_nav")
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.HOME,
            onClick = { onNavigate(Screen.HOME) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = ElectricCyan.copy(alpha = 0.2f),
                selectedIconColor = ElectricCyan,
                selectedTextColor = ElectricCyan
            )
        )

        NavigationBarItem(
            selected = currentScreen == Screen.TRANSACTIONS,
            onClick = { onNavigate(Screen.TRANSACTIONS) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.TRANSACTIONS) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                    contentDescription = "History"
                )
            },
            label = { Text("History") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = ElectricCyan.copy(alpha = 0.2f),
                selectedIconColor = ElectricCyan,
                selectedTextColor = ElectricCyan
            )
        )

        // Center Floating Scan & Pay Button
        NavigationBarItem(
            selected = currentScreen == Screen.SCAN_QR,
            onClick = { onNavigate(Screen.SCAN_QR) },
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(ElectricCyan, NeonPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan & Pay",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = { Text("Scan", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent,
                selectedTextColor = ElectricCyan
            )
        )

        NavigationBarItem(
            selected = currentScreen == Screen.WALLET,
            onClick = { onNavigate(Screen.WALLET) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.WALLET) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                    contentDescription = "Wallet"
                )
            },
            label = { Text("Wallet") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = ElectricCyan.copy(alpha = 0.2f),
                selectedIconColor = ElectricCyan,
                selectedTextColor = ElectricCyan
            )
        )

        NavigationBarItem(
            selected = currentScreen == Screen.PROFILE,
            onClick = { onNavigate(Screen.PROFILE) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = ElectricCyan.copy(alpha = 0.2f),
                selectedIconColor = ElectricCyan,
                selectedTextColor = ElectricCyan
            )
        )
    }
}
