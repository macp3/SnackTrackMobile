package study.snacktrackmobile.ui.components

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
import androidx.compose.material3.*
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import study.snacktrackmobile.R
import study.snacktrackmobile.repository.NotificationsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnackTrackTopBarCalendar(
    onDateSelected: (String) -> Unit
) {
    val montserratFont = FontFamily(Font(R.font.montserrat, weight = FontWeight.Normal))
    val scope = rememberCoroutineScope()
    val leftDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var rightDrawerOpen by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }

    // zakres - 1000 dni wstecz i 1000 do przodu
    val totalDays = 2001
    val days = remember {
        (0 until totalDays).map { today.minusDays(1000L - it) }
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 1000)

    // Stan dla miesiąca i roku widocznego na ekranie
    var visibleMonth by remember { mutableStateOf(today.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH).replaceFirstChar { it.uppercase() }) }
    var visibleYear by remember { mutableStateOf(today.year) }

    // Zmieniaj miesiąc i rok przy przewijaniu (ale NIE selectedDate)
    LaunchedEffect(listState.firstVisibleItemIndex) {
        val visibleIndex = listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size / 2
        val newVisibleDate = days.getOrNull(visibleIndex) ?: today
        visibleMonth = newVisibleDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            .replaceFirstChar { it.uppercase() }
        visibleYear = newVisibleDate.year
    }

    ModalNavigationDrawer(
        drawerState = leftDrawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(Color.White)
                    .padding(vertical = 24.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = "Menu",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                @Composable
                fun renderOption(title: String, action: () -> Unit) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clickable {
                                action()
                                scope.launch { leftDrawerState.close() }
                            }
                    )
                }

                renderOption("Home") { println("Go to Home") }
                renderOption("Profile") { println("Go to Profile") }
                renderOption("Progress") { println("Show progress") }
                renderOption("Settings") { println("Open settings") }
            }
        },
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFBFFF99))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // TopBar
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
                                .clickable { scope.launch { leftDrawerState.open() } },
                            tint = Color.Black
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SnackTrack",
                                fontFamily = montserratFont,
                                fontSize = 31.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
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
                                .clickable { rightDrawerOpen = true },
                            tint = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Nazwa miesiąca + rok (zmienia się przy przewijaniu)
                    Text(
                        text = "$visibleMonth $visibleYear",
                        fontFamily = montserratFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pasek dni (infinite scroll)
                    LazyRow(
                        state = listState,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        itemsIndexed(days) { _, date ->
                            val isSelected = date == selectedDate
                            val dayName = date.dayOfWeek
                                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                            val dayNumber = date.dayOfMonth

                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color(0xFF2E7D32) else Color.White
                                    )
                                    .clickable {
                                        selectedDate = date
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

                RightDrawer(drawerState = rightDrawerOpen, onClose = { rightDrawerOpen = false }) {
                    Text(
                        text = "Notifications",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    val notifications = NotificationsRepository.notifications

                    if (notifications.isEmpty()) {
                        Text(
                            text = "No notifications",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column {
                            notifications.forEach { notification ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = notification.title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = notification.body,
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                    HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
