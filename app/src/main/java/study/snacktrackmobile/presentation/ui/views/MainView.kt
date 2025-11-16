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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import study.snacktrackmobile.data.api.FoodApi
import study.snacktrackmobile.data.api.TrainingApi
import study.snacktrackmobile.data.model.Product
import study.snacktrackmobile.data.api.UserApi
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.network.ApiConfig
import study.snacktrackmobile.data.repository.NotificationsRepository
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.components.*
import study.snacktrackmobile.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    navController: NavController,
    shoppingListViewModel: ShoppingListViewModel,
    registeredAlimentationViewModel: RegisteredAlimentationViewModel,
    foodViewModel: FoodViewModel,
    loggedUserEmail: String,
    initialTab: String,
    initialMeal: String,
    initialDate: String
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedTab by remember { mutableStateOf(initialTab) }
    var selectedMeal by remember { mutableStateOf(initialMeal) }
    var rightDrawerOpen by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var alimentationToEdit by remember { mutableStateOf<RegisteredAlimentationResponse?>(null) }
    var selectedProduct by remember { mutableStateOf<RegisteredAlimentationResponse?>(null) }

    val leftDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var authToken by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        authToken = TokenStorage.getToken(context)
    }

    // Ustawienie użytkownika w ViewModel i inicjalizacja daty
    LaunchedEffect(loggedUserEmail) {
        shoppingListViewModel.setUser(loggedUserEmail)
        if (selectedTab == "Shopping") {
            shoppingListViewModel.setDate(selectedDate)
        }
    }

    // Training API + ViewModel
    val trainingApi = remember {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TrainingApi::class.java)
    }
    val trainingViewModel = remember { TrainingViewModel(trainingApi) }

    // Profile API + ViewModel
    val userApi = remember {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApi::class.java)
    }
    val profileViewModel = remember { ProfileViewModel(userApi) }

    val userTraining by remember { derivedStateOf { trainingViewModel.userTraining } }

    val showCalendar = when (selectedTab) {
        "Meals" -> true
        "Training" -> userTraining != null
        "Shopping" -> true
        else -> false
    }

    // Drawer lewy

    val navBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.savedStateHandle
            ?.getLiveData<Int>("editAlimentationId")
            ?.observeForever { id ->
                val product = registeredAlimentationViewModel.raw.value.find { it.id == id }
                alimentationToEdit = product
                isEditMode = true
                selectedTab = "AddProduct"
            }
    }



    ModalNavigationDrawer(
        drawerState = leftDrawerState,
        gesturesEnabled = !rightDrawerOpen,
        drawerContent = {
            DrawerContent(
                onClose = { scope.launch { leftDrawerState.close() } },
                onNavigate = { selectedTab = it }
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (showCalendar) {
                    SnackTrackTopBarCalendar(
                        selectedDate = selectedDate,
                        onDateSelected = { date ->
                            selectedDate = date
                            if (selectedTab == "Shopping") shoppingListViewModel.setDate(selectedDate)
                        },
                        onOpenMenu = { scope.launch { leftDrawerState.open() } },
                        onOpenNotifications = { rightDrawerOpen = true }
                    )
                } else {
                    SnackTrackTopBarWithIcons(
                        onOpenMenu = { scope.launch { leftDrawerState.open() } },
                        onOpenNotifications = { rightDrawerOpen = true }
                    )
                }
            },
            bottomBar = {
                Column {
                    if (selectedTab != "AddProduct" && selectedTab != "AddProductToDatabase") {
                        SummaryBar()
                    }
                    BottomNavigationBar(
                        selectedItem = selectedTab,
                        onItemSelected = { tab ->
                            selectedTab = tab
                            if (tab == "Shopping") shoppingListViewModel.setDate(selectedDate)
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize().background(Color.White)
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                when (selectedTab) {
                    "Meals" -> MealsDailyView(
                        selectedDate = selectedDate,
                        viewModel = registeredAlimentationViewModel,
                        navController = navController
                    )
                    "Training" -> TrainingView(
                        viewModel = trainingViewModel,
                        selectedDate = selectedDate,
                        authToken = authToken,
                        onDateSelected = { date -> selectedDate = date }
                    )
                    "Recipes" -> Text("Przepisy dnia $selectedDate")
                    "Shopping" -> ShoppingListScreen(
                        viewModel = shoppingListViewModel,
                        selectedDate = selectedDate
                    )
                    "Profile" -> ProfileScreen(
                        viewModel = profileViewModel,
                        onEditBodyParameters = { selectedTab = "EditProfile" }
                    )
                    "EditProfile" -> EditBodyParametersScreen(
                        viewModel = profileViewModel,
                        onBack = { selectedTab = "Profile" }
                    )
                    "AddProduct" -> {
                        if (selectedProduct == null) {
                            AddProductScreen(
                                selectedDate = selectedDate,
                                selectedMeal = selectedMeal,
                                navController = navController,
                                foodViewModel = foodViewModel,
                                onProductClick = { product -> selectedProduct = product }
                            )
                        } else {
                            ProductDetailsScreen(
                                alimentation = selectedProduct!!,
                                selectedDate = selectedDate,
                                selectedMeal = selectedMeal,
                                onBack = { selectedProduct = null },
                                registeredAlimentationViewModel = registeredAlimentationViewModel
                            )
                        }
                    }
                    "AddProductToDatabase" -> AddProductToDatabaseScreen(
                        navController = navController,
                        foodViewModel = foodViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Right drawer: Notifications
        if (rightDrawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { rightDrawerOpen = false },
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(320.dp)
                        .background(Color.White)
                        .padding(16.dp)
                        .clickable(enabled = false) {}
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Notifications", style = MaterialTheme.typography.titleLarge, color = Color(0xFF4CAF50))
                            IconButton(onClick = { rightDrawerOpen = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        val notifications = NotificationsRepository.notifications

                        if (notifications.isEmpty()) {
                            Text("No new notification", color = Color.Gray)
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(notifications.size) { index ->
                                    val notification = notifications[index]
                                    NotificationItem(
                                        title = notification.title,
                                        body = notification.body,
                                        onDelete = { NotificationsRepository.removeNotification(notification) }
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

@Composable
fun DrawerContent(onClose: () -> Unit, onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color.White)
            .padding(24.dp)
    ) {
        Text("Meals", modifier = Modifier.clickable { onNavigate("Meals"); onClose() })
        Spacer(modifier = Modifier.height(12.dp))
        Text("Training", modifier = Modifier.clickable { onNavigate("Training"); onClose() })
        Spacer(modifier = Modifier.height(12.dp))
        Text("Shopping", modifier = Modifier.clickable { onNavigate("Shopping"); onClose() })
        Spacer(modifier = Modifier.height(12.dp))
        Text("Profile", modifier = Modifier.clickable { onNavigate("Profile"); onClose() })
    }
}
