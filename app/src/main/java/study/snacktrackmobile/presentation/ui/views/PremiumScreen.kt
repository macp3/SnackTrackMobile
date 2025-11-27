package study.snacktrackmobile.presentation.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.snacktrackmobile.data.model.User
import java.time.LocalDate

fun isPremiumActive(premiumExpiration: String?): Boolean {
    if (premiumExpiration == null) return false
    return try {
        val expiration = LocalDate.parse(premiumExpiration)
        expiration.isAfter(LocalDate.now())
    } catch (e: Exception) {
        false
    }
}

fun fakePurchase(months: Int): String {
    val today = LocalDate.now()
    val newDate = today.plusMonths(months.toLong())
    return newDate.toString() // Format: YYYY-MM-DD
}

@Composable
fun SubscriptionCard(
    title: String,
    price: String,
    description: String,
    isBestOffer: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isBestOffer) Color(0xFFFFF7DA)
                else Color(0xFFF3F3F3)
            )
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    fontSize = 22.sp,
                    fontFamily = montserratFont,
                    color = Color.Black
                )

                if (isBestOffer) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFC107))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("BEST", fontSize = 12.sp, color = Color.Black)
                    }
                }
            }

            Text(price, fontSize = 20.sp, color = Color(0xFF2E7D32))
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, fontSize = 15.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun PremiumScreen(
    user: User?,
    onPremiumActivated: (String) -> Unit,
    onExtendPremium: (String) -> Unit
) {
    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...")
        }
        return
    }

    val hasPremium = isPremiumActive(user.premiumExpiration)

    // 🔹 1. Tworzymy stan przewijania
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState) // 🔹 2. Włączamy przewijanie
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFFFC107),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (hasPremium) {
            // -----------------------------------
            //             PREMIUM ACTIVE
            // -----------------------------------
            Text(
                "Premium Active",
                fontSize = 28.sp,
                fontFamily = montserratFont,
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Valid until: ${user.premiumExpiration}",
                fontSize = 18.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Extend your premium:",
                fontSize = 20.sp,
                fontFamily = montserratFont,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            SubscriptionCard(
                title = "1 Month",
                price = "$4.99",
                description = "Extend Premium by 30 days",
                onClick = {
                    val newDate = fakePurchase(1)
                    onExtendPremium(newDate)
                }
            )

            SubscriptionCard(
                title = "3 Months",
                price = "$12.99",
                description = "Save 13% compared to monthly",
                onClick = {
                    val newDate = fakePurchase(3)
                    onExtendPremium(newDate)
                }
            )

            SubscriptionCard(
                title = "12 Months",
                price = "$39.99",
                description = "Best price! Save 33%",
                isBestOffer = true,
                onClick = {
                    val newDate = fakePurchase(12)
                    onExtendPremium(newDate)
                }
            )

        } else {
            // -----------------------------------
            //             NO PREMIUM
            // -----------------------------------
            Text(
                "Upgrade to Premium",
                fontSize = 28.sp,
                fontFamily = montserratFont,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Unlock exclusive features, advanced analytics, training plans and more!",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            SubscriptionCard(
                title = "1 Month",
                price = "$4.99",
                description = "Full Premium access for 30 days",
                onClick = {
                    val newDate = fakePurchase(1)
                    onPremiumActivated(newDate)
                }
            )

            SubscriptionCard(
                title = "3 Months",
                price = "$12.99",
                description = "Save 13% vs monthly",
                onClick = {
                    val newDate = fakePurchase(3)
                    onPremiumActivated(newDate)
                }
            )

            SubscriptionCard(
                title = "12 Months",
                price = "$39.99",
                description = "Best value: save 33%!",
                isBestOffer = true,
                onClick = {
                    val newDate = fakePurchase(12)
                    onPremiumActivated(newDate)
                }
            )
        }

        // Dodatkowy odstęp na dole, żeby ostatni element nie przylegał do krawędzi
        Spacer(modifier = Modifier.height(24.dp))
    }
}