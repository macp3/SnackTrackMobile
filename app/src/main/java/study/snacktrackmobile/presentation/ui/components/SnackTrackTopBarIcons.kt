package study.snacktrackmobile.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import study.snacktrackmobile.R
import study.snacktrackmobile.data.repository.NotificationsRepository

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


@Composable
fun SnackTrackTopBarWithIcons(
    onOpenMenu: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val montserratFont = FontFamily(Font(R.font.montserrat, weight = FontWeight.Normal))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFBFFF99))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Lewy hamburger
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = "Menu",
            modifier = Modifier
                .size(24.dp)
                .clickable { onOpenMenu() },
            tint = Color.Black
        )

        // Tytuł w centrum
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "SnackTrack",
                fontFamily = montserratFont,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )
            Text(
                text = "Track your progress",
                fontFamily = montserratFont,
                fontSize = 14.sp,
                color = Color.Black
            )
        }

        // Prawy drawer (powiadomienia)
        Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = "Notifications",
            modifier = Modifier
                .size(24.dp)
                .clickable { onOpenNotifications() },
            tint = Color.Black
        )
    }
}


@Composable
fun RightDrawer(
    drawerState: Boolean,
    onClose: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (drawerState) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000))
                    .clickable { onClose() }
            )
        }

        AnimatedVisibility(
            visible = drawerState,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Column(
                modifier = Modifier
                    .width(240.dp) // zmniejszona szerokość drawer
                    .fillMaxHeight()
                    .background(Color.White)
                    .padding(12.dp), // mniejszy padding
                content = content
            )
        }
    }
}

