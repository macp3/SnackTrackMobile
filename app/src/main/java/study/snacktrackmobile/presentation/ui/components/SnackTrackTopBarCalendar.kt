package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import study.snacktrackmobile.R

@Composable
fun SnackTrackTopBarCalendar(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onOpenMenu: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val montserratFont = FontFamily(Font(R.font.montserrat, weight = FontWeight.Normal))
    val today = LocalDate.now()

    val totalDays = 2001
    val days = remember { (0 until totalDays).map { today.minusDays(1000L - it) } }
    val todayIndex = 1000
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = todayIndex)

    var visibleMonth by remember { mutableStateOf(today.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)) }
    var visibleYear by remember { mutableStateOf(today.year) }

    LaunchedEffect(listState.firstVisibleItemIndex) {
        val visibleIndex = listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size / 2
        val newVisibleDate = days.getOrNull(visibleIndex) ?: today
        visibleMonth = newVisibleDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            .replaceFirstChar { it.uppercase() }
        visibleYear = newVisibleDate.year
    }

    LaunchedEffect(selectedDate) {
        val selectedLocalDate = try {
            LocalDate.parse(selectedDate)
        } catch (e: Exception) {
            today
        }
        if (selectedLocalDate == today) {
            listState.animateScrollToItem(todayIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFBFFF99))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Menu",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onOpenMenu() },
                tint = Color.Black
            )

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

            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Notifications",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onOpenNotifications() },
                tint = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Widoczny miesiąc i rok
        Text(
            text = "${visibleMonth.replaceFirstChar { it.uppercase() }} $visibleYear",
            fontFamily = montserratFont,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            itemsIndexed(days) { _, date ->
                val isSelected = date == try {
                    LocalDate.parse(selectedDate)
                } catch (e: Exception) {
                    today
                }

                val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                val dayNumber = date.dayOfMonth

                Box(
                    modifier = Modifier
                        .size(43.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF2E7D32) else Color.White)
                        .clickable { onDateSelected(date.format(DateTimeFormatter.ISO_DATE)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$dayName\n$dayNumber",
                        fontFamily = montserratFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.Black,
                        lineHeight = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

            }
        }


    }
}
