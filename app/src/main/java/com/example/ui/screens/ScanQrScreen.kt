package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MintGreen
import com.example.ui.theme.NeonPurple

data class QuickMerchantQr(
    val name: String,
    val upiId: String,
    val amount: String,
    val category: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val testMerchants = listOf(
    QuickMerchantQr("Starbucks Coffee", "starbucks@icici", "280", "Food", Icons.Default.LocalCafe),
    QuickMerchantQr("Swiggy Delivery", "swiggy@hdfcbank", "450", "Food", Icons.Default.Fastfood),
    QuickMerchantQr("Kirana Supermart", "kirana@paytm", "120", "Grocery", Icons.Default.LocalGroceryStore),
    QuickMerchantQr("Metro Travel Pass", "metro@sbi", "60", "Travel", Icons.Default.DirectionsTransit),
    QuickMerchantQr("Zomato Treats", "zomato@axisbank", "320", "Food", Icons.Default.DinnerDining)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanQrScreen(
    onQrScanned: (name: String, upiId: String, amount: String, category: String) -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }

    var isTorchOn by remember { mutableStateOf(false) }

    // Laser Animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Any UPI QR Code", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isTorchOn = !isTorchOn }) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Torch",
                            tint = if (isTorchOn) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF090C15)
                )
            )
        },
        containerColor = Color(0xFF090C15)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .testTag("scan_qr_screen"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Align QR code inside the viewfinder",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Scanner Viewfinder Box
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF131A2D))
                    .border(2.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Laser Scanner Canvas
                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val laserY = size.height * laserOffset
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, ElectricCyan, Color.White, ElectricCyan, Color.Transparent)
                        ),
                        start = Offset(0f, laserY),
                        end = Offset(size.width, laserY),
                        strokeWidth = 3.dp.toPx()
                    )
                }

                // Decorative Center Frame
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = ElectricCyan.copy(alpha = 0.35f),
                    modifier = Modifier.size(90.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "⚡ Instant Test Merchants",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
            Text(
                text = "Tap any merchant below to simulate scanning their live QR code",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                testMerchants.forEach { merchant ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF151D30),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFF263352), Color(0xFF1A2238)))),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onQrScanned(merchant.name, merchant.upiId, merchant.amount, merchant.category)
                            }
                            .testTag("merchant_${merchant.name.replace(" ", "_")}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(ElectricCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = merchant.icon,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = merchant.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = merchant.upiId,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹${merchant.amount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MintGreen
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Scan",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
