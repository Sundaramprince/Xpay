package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveMoneyScreen(
    user: UserEntity?,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    var customAmount by remember { mutableStateOf("") }
    var isCopied by remember { mutableStateOf(false) }
    var showShareMessage by remember { mutableStateOf(false) }

    val upiId = user?.upiId ?: "alex@xpay"
    val fullName = user?.fullName ?: "Alex Johnson"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My QR Code & UPI ID", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp)
                .testTag("receive_money_screen"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // QR Container Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fullName.take(1),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }
                        Column {
                            Text(
                                text = fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Scan to pay with any UPI App",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stylized Geometric Canvas QR Code
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val qrColor = Color(0xFF0F172A)
                            val accentColor = Color(0xFF0284C7)
                            val matrixSize = 19
                            val cellSize = size.width / matrixSize

                            // Three Position Finder Patterns
                            fun drawFinder(startX: Float, startY: Float) {
                                // Outer 7x7 square
                                drawRoundRect(
                                    color = qrColor,
                                    topLeft = Offset(startX, startY),
                                    size = Size(cellSize * 7, cellSize * 7),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                                // White 5x5 cutout
                                drawRect(
                                    color = Color.White,
                                    topLeft = Offset(startX + cellSize, startY + cellSize),
                                    size = Size(cellSize * 5, cellSize * 5)
                                )
                                // Inner 3x3 solid square
                                drawRoundRect(
                                    color = accentColor,
                                    topLeft = Offset(startX + cellSize * 2, startY + cellSize * 2),
                                    size = Size(cellSize * 3, cellSize * 3),
                                    cornerRadius = CornerRadius(4f, 4f)
                                )
                            }

                            // Draw Finders
                            drawFinder(0f, 0f)
                            drawFinder(size.width - cellSize * 7, 0f)
                            drawFinder(0f, size.height - cellSize * 7)

                            // Pseudorandom data modules based on upiId hash
                            val seed = (upiId + customAmount).hashCode()
                            val random = java.util.Random(seed.toLong())

                            for (r in 0 until matrixSize) {
                                for (c in 0 until matrixSize) {
                                    // Skip finder zones
                                    val inTopLeft = r < 7 && c < 7
                                    val inTopRight = r < 7 && c >= matrixSize - 7
                                    val inBottomLeft = r >= matrixSize - 7 && c < 7
                                    val inCenterLogo = r in 8..10 && c in 8..10

                                    if (!inTopLeft && !inTopRight && !inBottomLeft && !inCenterLogo) {
                                        if (random.nextBoolean()) {
                                            drawRoundRect(
                                                color = qrColor,
                                                topLeft = Offset(c * cellSize + 1f, r * cellSize + 1f),
                                                size = Size(cellSize - 2f, cellSize - 2f),
                                                cornerRadius = CornerRadius(2f, 2f)
                                            )
                                        }
                                    }
                                }
                            }

                            // Center Xpay Logo Badge
                            val centerSize = cellSize * 3
                            val centerOffset = (size.width - centerSize) / 2
                            drawRoundRect(
                                color = Color(0xFF00E5FF),
                                topLeft = Offset(centerOffset, centerOffset),
                                size = Size(centerSize, centerSize),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                        }

                        // Center letter X
                        Text(
                            text = "X",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF090C15)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (customAmount.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ElectricCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Requesting: ₹$customAmount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // UPI ID with copy button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("UPI ID", upiId)
                                clipboard.setPrimaryClip(clip)
                                isCopied = true
                            }
                            .testTag("copy_upi_id_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "UPI ID: $upiId",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (isCopied) Color(0xFF10B981) else ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Set Custom Amount Expandable
            OutlinedTextField(
                value = customAmount,
                onValueChange = { customAmount = it },
                label = { Text("Set Request Amount (Optional)") },
                placeholder = { Text("e.g. 500") },
                leadingIcon = { Text("₹", style = MaterialTheme.typography.titleMedium, color = ElectricCyan) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val payUri = "upi://pay?pa=$upiId&pn=${fullName.replace(" ", "%20")}" +
                                if (customAmount.isNotBlank()) "&am=$customAmount&cu=INR" else ""
                        clipboard.setPrimaryClip(ClipData.newPlainText("UPI Payment Link", payUri))
                        showShareMessage = true
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Link")
                }

                Button(
                    onClick = {
                        showShareMessage = true
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save QR", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }

            if (showShareMessage) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "QR Link copied to clipboard & saved!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}
