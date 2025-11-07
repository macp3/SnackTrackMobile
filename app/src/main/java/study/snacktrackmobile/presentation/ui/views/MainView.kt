package study.snacktrackmobile.presentation.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.repository.NotificationsRepository
import study.snacktrackmobile.presentation.ui.components.BottomNavigationBar
import study.snacktrackmobile.presentation.ui.components.NotificationItem
import study.snacktrackmobile.presentation.ui.components.ShoppingListScreen
import study.snacktrackmobile.presentation.ui.components.ShoppingListViewModel
import study.snacktrackmobile.presentation.ui.components.SnackTrackTopBarCalendar
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(navController: NavController,
             shoppingListViewModel: ShoppingListViewModel
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var selectedTab by remember { mutableStateOf("Meals") }

    val leftDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var rightDrawerOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = leftDrawerState,
        gesturesEnabled = !rightDrawerOpen,
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
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Text(
                    text = "Home",
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable { scope.launch { leftDrawerState.close() } }
                )
                Text(
                    text = "Settings",
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable { scope.launch { leftDrawerState.close() } }
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // 🔝 TopBar z kalendarzem
            Column(modifier = Modifier.fillMaxSize()) {
                SnackTrackTopBarCalendar(
                    onDateSelected = { date -> selectedDate = date },
                    onOpenMenu = {
                        if (!rightDrawerOpen) scope.launch { leftDrawerState.open() }
                    },
                    onOpenNotifications = {
                        scope.launch { leftDrawerState.close() }
                        rightDrawerOpen = true
                    }
                )

                // 📦 Główna zawartość
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (selectedTab) {
                        "Meals" -> Text("Zawartość posiłków na dzień $selectedDate")
                        "Training" -> Text("Treningi dla daty $selectedDate")
                        "Recipes" -> Text("Przepisy dnia $selectedDate")
                        "Shopping" -> {
                            ShoppingListScreen(viewModel = shoppingListViewModel)
                        }
                        "Profile" -> Text("Twój profil (data: $selectedDate)")
                        else -> Text("Wybierz sekcję i datę")
                    }
                }

                // 🔻 Dolny pasek nawigacji
                BottomNavigationBar(
                    selectedItem = selectedTab,
                    onItemSelected = { tab -> selectedTab = tab }
                )
            }

            // 🟢 Overlay zamykający panel po kliknięciu poza nim
            if (rightDrawerOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { rightDrawerOpen = false },
                    contentAlignment = Alignment.CenterEnd
                ) {
                    // Panel powiadomień (kliknięcia w niego NIE zamykają panelu)
                    // Panel powiadomień (kliknięcia w niego NIE zamykają panelu)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(320.dp)
                            .background(Color.White)
                            .padding(16.dp)
                            .clickable(enabled = false) {} // blokuje "przepuszczanie" kliknięcia
                    ) {
                        Column {
                            // Nagłówek
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Notifications",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color(0xFF4CAF50)
                                )
                                IconButton(
                                    onClick = { rightDrawerOpen = false }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close notification",
                                        tint = Color.Gray
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            val notifications = NotificationsRepository.notifications

                            if (notifications.isEmpty()) {
                                Text(
                                    text = "No new notification",
                                    color = Color.Gray
                                )
                            } else {
                                // LazyColumn dla scrollowania dużych list
                                LazyColumn(
                                    modifier = Modifier.fillMaxHeight(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(notifications.size) { index ->
                                        val notification = notifications[index]
                                        NotificationItem(
                                            title = notification.title,
                                            body = notification.body,
                                            onDelete = {
                                                NotificationsRepository.removeNotification(notification)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}