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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import study.snacktrackmobile.data.api.TrainingApi
import study.snacktrackmobile.data.network.ApiConfig
import study.snacktrackmobile.data.repository.NotificationsRepository
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.components.BottomNavigationBar
import study.snacktrackmobile.presentation.ui.components.MealsDailyView
import study.snacktrackmobile.presentation.ui.components.NotificationItem
import study.snacktrackmobile.presentation.ui.components.ShoppingListScreen
import study.snacktrackmobile.viewmodel.ShoppingListViewModel
import study.snacktrackmobile.presentation.ui.components.SnackTrackTopBarCalendar
import study.snacktrackmobile.presentation.ui.components.SummaryBar
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel
import study.snacktrackmobile.viewmodel.TrainingViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(navController: NavController,
             shoppingListViewModel: ShoppingListViewModel,
             registeredAlimentationViewModel: RegisteredAlimentationViewModel,
             loggedUserEmail: String
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var selectedTab by remember { mutableStateOf("Meals") }

    val leftDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var rightDrawerOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    var authToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        authToken = TokenStorage.getToken(context)
    }

    val trainingApi = remember {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TrainingApi::class.java)
    }

    val trainingViewModel = remember {
        TrainingViewModel(api = trainingApi)
    }

    LaunchedEffect(loggedUserEmail) {
        shoppingListViewModel.setUser(loggedUserEmail)
        if (selectedTab == "Shopping") {
            shoppingListViewModel.setDate(selectedDate)
        }
    }

    // 🔹 główny kontener z lewym menu
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
                    modifier = Modifier.padding(bottom = 24.dp),
                    fontFamily = montserratFont
                )
                Text(
                    text = "Home",
                    fontFamily = montserratFont,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable { scope.launch { leftDrawerState.close() } }
                )
                Text(
                    text = "Settings",
                    fontFamily = montserratFont,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable { scope.launch { leftDrawerState.close() } }
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

                SnackTrackTopBarCalendar(
                    selectedDate = selectedDate,
                    onDateSelected = { date -> selectedDate = date
                        if (selectedTab == "Shopping") {
                            shoppingListViewModel.setDate(date)
                        }
                                     },
                    onOpenMenu = {
                        if (!rightDrawerOpen) scope.launch { leftDrawerState.open() }
                    },
                    onOpenNotifications = {
                        scope.launch { leftDrawerState.close() }
                        rightDrawerOpen = true
                    }
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
                        "Training" -> TrainingView(
                            viewModel = trainingViewModel,
                            selectedDate = selectedDate,
                            authToken = authToken,
                            onDateSelected = { date -> selectedDate = date }
                        )
                        "Recipes" -> Text("Przepisy dnia $selectedDate")
                        "Shopping" -> {
                            ShoppingListScreen(
                                viewModel = shoppingListViewModel,
                                selectedDate = selectedDate
                            )
                        }
                        "Profile" -> Text("Twój profil (data: $selectedDate)")
                    }
                }

                // ✅ SummaryBar zawsze widoczny nad bottom nav
                SummaryBar()

                BottomNavigationBar(
                    selectedItem = selectedTab,
                    onItemSelected = { tab ->
                        selectedTab = tab
                        if (tab == "Shopping") {
                            shoppingListViewModel.setDate(selectedDate)
                        }
                    }
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