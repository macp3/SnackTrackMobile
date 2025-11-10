package study.snacktrackmobile.presentation.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import study.snacktrackmobile.presentation.ui.components.AddProductScreen
import study.snacktrackmobile.presentation.ui.components.BottomNavigationBar
import study.snacktrackmobile.presentation.ui.components.MealsDailyView
import study.snacktrackmobile.presentation.ui.components.NotificationItem
import study.snacktrackmobile.presentation.ui.components.ShoppingListScreen
import study.snacktrackmobile.viewmodel.ShoppingListViewModel
import study.snacktrackmobile.presentation.ui.components.SnackTrackTopBarCalendar
import study.snacktrackmobile.presentation.ui.components.SummaryBar
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(navController: NavController,
             shoppingListViewModel: ShoppingListViewModel,
             registeredAlimentationViewModel: RegisteredAlimentationViewModel,
             loggedUserEmail: String,
             initialTab: String,
             initialMeal: String,
             initialDate: String
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedTab by remember { mutableStateOf(initialTab) }
    var selectedMeal by remember { mutableStateOf(initialMeal) }
    var rightDrawerOpen by remember { mutableStateOf(false) }

    val leftDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Zakładamy, że montserratFont jest dostępny
    val montserratFont = androidx.compose.ui.text.font.FontFamily.Default

    LaunchedEffect(loggedUserEmail, selectedDate) {
        shoppingListViewModel.setUser(loggedUserEmail)
        if (selectedTab == "Shopping") {
            shoppingListViewModel.setDate(selectedDate)
        }
    }

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
        Scaffold(
            topBar = {
                SnackTrackTopBarCalendar(
                    onDateSelected = { date ->
                        selectedDate = date
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
            },
            bottomBar = {
                Column {
                    // ✅ Warunkowe renderowanie SUMMARYBAR
                    if (selectedTab != "AddProduct") {
                        SummaryBar()
                    }
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
            },
            modifier = Modifier.fillMaxSize().background(Color.White)
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Padding od barów
                    // ✅ KOREKTA: Zastosuj białe tło, zanim dodasz paddingi
                    .background(Color.White)
                    // ✅ KOREKTA: Dodaj dodatkowy 10.dp padding, teraz na białym tle
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                when (selectedTab) {
                    "Meals" -> MealsDailyView(
                        selectedDate = selectedDate,
                        viewModel = registeredAlimentationViewModel,
                        navController
                    )
                    "Training" -> Text("Treningi dla daty $selectedDate")
                    "Recipes" -> Text("Przepisy dnia $selectedDate")
                    "Shopping" -> {
                        ShoppingListScreen(
                            viewModel = shoppingListViewModel,
                            selectedDate = selectedDate
                        )
                    }
                    "Profile" -> Text("Twój profil (data: $selectedDate")
                    "AddProduct" -> AddProductScreen(
                        selectedDate = selectedDate,
                        selectedMeal = selectedMeal,
                        navController = navController
                    )
                }
            }
        }

        // Panel powiadomień (Right Drawer)
        if (rightDrawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { rightDrawerOpen = false },
                contentAlignment = Alignment.CenterEnd
            ) {
                // Panel powiadomień
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(320.dp)
                        .background(Color.White)
                        .padding(16.dp)
                        .clickable(enabled = false) {}
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