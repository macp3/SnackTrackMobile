package study.snacktrackmobile.presentation.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.UserViewModel

@Composable
fun DrawerContent(
    onClose: () -> Unit,
    onNavigate: (String) -> Unit,
    userViewModel: UserViewModel,
    context: Context,
    onLoggedOut: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color.White)
            .padding(vertical = 32.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onNavigate("Premium")
                    onClose()
                }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(Color(0xFFFFC107), Color(0xFFFFA000))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = "Premium", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))

            Box(modifier = Modifier) {
                Text(
                    "Premium",
                    fontSize = 18.sp,
                    fontFamily = montserratFont,
                    color = Color(0xFF2E7D32)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 30.dp)
                        .background(Color(0xFF81C784), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("Pro", fontSize = 9.sp, color = Color.White, fontFamily = montserratFont)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Menu", fontSize = 24.sp, fontFamily = montserratFont, color = Color(0xFF2E7D32))
        Spacer(modifier = Modifier.height(24.dp))

        @Composable
        fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(); onClose() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFE0F2F1), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = label, tint = Color(0xFF2E7D32))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(label, fontSize = 18.sp, fontFamily = montserratFont, color = Color.Black)
            }
        }

        DrawerItem(Icons.Default.Restaurant, "Meals") { onNavigate("Meals") }
        DrawerItem(Icons.Default.FitnessCenter, "Training") { onNavigate("Training") }
        DrawerItem(Icons.Default.Book, "Recipes") { onNavigate("Recipes") }
        DrawerItem(Icons.Default.ShoppingCart, "Shopping") { onNavigate("Shopping") }
        DrawerItem(Icons.Default.Person, "Profile") { onNavigate("Profile") }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        DrawerItem(Icons.Default.Info, "About Us") { onNavigate("AboutUs") }
        DrawerItem(Icons.AutoMirrored.Filled.ExitToApp, "Log Out") { showLogoutDialog = true }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out", fontFamily = montserratFont) },
            text = { Text("Are you sure you want to log out?", fontFamily = montserratFont) },
            confirmButton = {
                TextButton(onClick = {
                    userViewModel.logout(context)
                    showLogoutDialog = false
                    onLoggedOut()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}
