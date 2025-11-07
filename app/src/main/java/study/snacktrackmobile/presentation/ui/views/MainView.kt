package study.snacktrackmobile.presentation.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.repository.NotificationsRepository
import study.snacktrackmobile.presentation.ui.components.BottomNavigationBar
import study.snacktrackmobile.presentation.ui.components.MealsDailyView
import study.snacktrackmobile.presentation.ui.components.SnackTrackTopBarCalendar
import study.snacktrackmobile.presentation.ui.components.SummaryBar
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(navController: NavController, registeredAlimentationViewModel: RegisteredAlimentationViewModel) {
    var selectedDate by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Meals") }

    val leftDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var rightDrawerOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 🔹 główny kontener z lewym menu
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
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Text(
                    text = "Home",
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable {
                            scope.launch { leftDrawerState.close() }
                        }
                )
                Text(
                    text = "Settings",
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable {
                            scope.launch { leftDrawerState.close() }
                        }
                )
            }
        }
    ) {
        Box {
            Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

                SnackTrackTopBarCalendar(
                    onDateSelected = { date -> selectedDate = date },
                    onOpenMenu = { scope.launch { leftDrawerState.open() } },
                    onOpenNotifications = { rightDrawerOpen = true }
                )

                // Cała główna scrollowana zawartość
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 10.dp, top = 10.dp)
                ) {
                    when (selectedTab) {
                        "Meals" -> MealsDailyView(
                            selectedDate = selectedDate,
                            viewModel = registeredAlimentationViewModel
                        )
                        "Training" -> Text("Treningi dla daty $selectedDate")
                        "Recipes" -> Text("Przepisy dnia $selectedDate")
                        "Shopping" -> Text("Lista zakupów na $selectedDate")
                        "Profile" -> Text("Twój profil (data: $selectedDate)")
                    }
                }

                // ✅ SummaryBar zawsze widoczny nad bottom nav
                SummaryBar()

                BottomNavigationBar(
                    selectedItem = selectedTab,
                    onItemSelected = { tab -> selectedTab = tab }
                )
            }

            // 🔔 Panel powiadomień z repozytorium
            if (rightDrawerOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(300.dp)
                        .align(Alignment.CenterEnd)
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Column {
                        // Górny pasek panelu powiadomień
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Powiadomienia",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                "Zamknij",
                                color = Color.Gray,
                                modifier = Modifier.clickable { rightDrawerOpen = false }
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        val notifications = NotificationsRepository.notifications

                        if (notifications.isEmpty()) {
                            Text(
                                text = "Brak nowych powiadomień",
                                color = Color.Gray
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
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = notification.body,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.DarkGray
                                        )
                                        Divider(modifier = Modifier.padding(top = 6.dp))
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
