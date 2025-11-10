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

    // Zakres 2001 dni (1000 wstecz i 1000 do przodu)
    val totalDays = 2001
    val days = remember { (0 until totalDays).map { today.minusDays(1000L - it) } }
    val todayIndex = 1000
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = todayIndex)

    var visibleMonth by remember { mutableStateOf(today.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)) }
    var visibleYear by remember { mutableStateOf(today.year) }

    // Aktualizacja widocznego miesiąca i roku przy scrollu
    LaunchedEffect(listState.firstVisibleItemIndex) {
        val visibleIndex = listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size / 2
        val newVisibleDate = days.getOrNull(visibleIndex) ?: today
        visibleMonth = newVisibleDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            .replaceFirstChar { it.uppercase() }
        visibleYear = newVisibleDate.year
    }

    // Scroll do dzisiaj, jeśli selectedDate jest dzisiejszy
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 🔝 Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Menu",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onOpenMenu() },
                tint = Color.Black
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SnackTrack",
                    fontFamily = montserratFont,
                    fontSize = 31.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                Text(
                    text = "Track your progress",
                    fontFamily = montserratFont,
                    fontSize = 17.sp,
                    color = Color.Black
                )
            }

            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Notifications",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onOpenNotifications() },
                tint = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 📅 Widoczny miesiąc i rok
        Text(
            text = "${visibleMonth.replaceFirstChar { it.uppercase() }} $visibleYear",
            fontFamily = montserratFont,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 🔢 Pasek dni
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
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
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF2E7D32) else Color.White)
                        .clickable {
                            onDateSelected(date.format(DateTimeFormatter.ISO_DATE))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dayName,
                            fontFamily = montserratFont,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else Color.Black
                        )
                        Text(
                            text = dayNumber.toString(),
                            fontFamily = montserratFont,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }
        }
    }
}
