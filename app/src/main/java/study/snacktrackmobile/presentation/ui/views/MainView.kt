package study.snacktrackmobile.presentation.ui.views

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import study.snacktrackmobile.data.api.FoodApi
import study.snacktrackmobile.data.api.RecipeApi
import study.snacktrackmobile.data.api.TrainingApi
import study.snacktrackmobile.data.api.UserApi
import study.snacktrackmobile.data.model.User
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.model.dto.UserResponse
import study.snacktrackmobile.data.model.enums.Status
import study.snacktrackmobile.data.network.ApiConfig
import study.snacktrackmobile.data.repository.NotificationsRepository
import study.snacktrackmobile.data.repository.RecipeRepository
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.components.*
import study.snacktrackmobile.presentation.ui.components.RecipesScreen
import study.snacktrackmobile.viewmodel.*
import kotlin.jvm.java

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    navController: NavController,
    shoppingListViewModel: ShoppingListViewModel,
    registeredAlimentationViewModel: RegisteredAlimentationViewModel,
    foodViewModel: FoodViewModel,
    userViewModel: UserViewModel,
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

    // Recipe API + Repo + ViewModel
    val recipeApi = remember {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecipeApi::class.java)
    }

    val recipeRepository = remember { RecipeRepository(recipeApi) }
    val recipesViewModel: RecipeViewModel = viewModel(
        factory = RecipeViewModel.provideFactory(recipeRepository)
    )


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

    val userResponse by profileViewModel.user.collectAsState()

    LaunchedEffect(authToken) {
        authToken?.let { token ->
            profileViewModel.loadProfile(token)
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == "Recipes") {
            authToken?.let { token ->
                recipesViewModel.loadAllRecipes(token)
            }
        }
    }


    // Drawer lewy
    ModalNavigationDrawer(
        drawerState = leftDrawerState,
        gesturesEnabled = !rightDrawerOpen,
        drawerContent = {
            DrawerContent(
                onClose = { scope.launch { leftDrawerState.close() } },
                onNavigate = { selectedTab = it },
                onSettings = { navController.navigate("SettingsView") },
                onAboutUs = { navController.navigate("AboutUsView") },
                userViewModel = userViewModel,
                context = context,
                onLoggedOut = { navController.navigate("StartView") { popUpTo("MainView") { inclusive = true } } }
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
                        navController = navController,
                        onEditProduct = { product ->
                            selectedProduct = product
                            isEditMode = true
                            selectedTab = "AddProduct"
                        }
                    )
                    "Training" -> TrainingView(
                        viewModel = trainingViewModel,
                        selectedDate = selectedDate,
                        authToken = authToken,
                        onDateSelected = { date -> selectedDate = date }
                    )
                    "Recipes" -> RecipesScreen(
                        viewModel = recipesViewModel,
                        foodViewModel = foodViewModel, // 🔹 Przekazujemy FoodViewModel
                        navController = navController, // 🔹 Przekazujemy NavController
                    )

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
                                onProductClick = { product ->
                                    // tymczasowy produkt z AddProductScreen ma id = -1
                                    selectedProduct = product
                                    isEditMode = false
                                }
                            )
                        } else {
                            ProductDetailsScreen(
                                alimentation = selectedProduct!!,
                                selectedDate = selectedDate,
                                selectedMeal = selectedMeal,
                                onBack = {
                                    selectedProduct = null
                                    isEditMode = false
                                },
                                registeredAlimentationViewModel = registeredAlimentationViewModel,
                                isEditMode = selectedProduct!!.id > 0 // 🔹 PUT tylko dla istniejących
                            )
                        }
                    }
                    "AddProductToDatabase" -> AddProductToDatabaseScreen(
                        navController = navController,
                        foodViewModel = foodViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    "Premium" -> PremiumScreen(
                        user = userResponse?.toUser(),
                        onPremiumActivated = { newDate ->
                            authToken?.let { token ->
                                profileViewModel.updatePremium(token, newDate)
                            }
                        },
                        onExtendPremium = { newDate ->
                            authToken?.let { token ->
                                profileViewModel.updatePremium(token, newDate)
                            }
                        }
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

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

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
fun DrawerContent(
    onClose: () -> Unit,
    onNavigate: (String) -> Unit,
    onSettings: () -> Unit,
    onAboutUs: () -> Unit,
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
        // Premium z gradientem i badge "Pro"
        // Premium z ikoną i badge "Pro"
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
            // Ikona gwiazdki z gradientem
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

            // Tekst Premium z badge
            Box(modifier = Modifier) {
                Text(
                    "Premium",
                    fontSize = 18.sp,
                    fontFamily = montserratFont,
                    color = Color(0xFF2E7D32)
                )

                // Badge "Pro" mniejszy, bliżej tekstu i stonowany
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 30.dp, y = 0.dp) // bliżej tekstu
                        .background(Color(0xFF81C784), shape = RoundedCornerShape(4.dp)) // bardziej stonowany zielony
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        "Pro",
                        fontSize = 9.sp,
                        color = Color.White,
                        fontFamily = montserratFont
                    )
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

        // Główne sekcje
        DrawerItem(Icons.Default.Restaurant, "Meals") { onNavigate("Meals") }
        DrawerItem(Icons.Default.FitnessCenter, "Training") { onNavigate("Training") }
        DrawerItem(Icons.Default.Book, "Recipes") { onNavigate("Recipes") }
        DrawerItem(Icons.Default.ShoppingCart, "Shopping") { onNavigate("Shopping") }
        DrawerItem(Icons.Default.Person, "Profile") { onNavigate("Profile") }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Dodatkowe opcje
        DrawerItem(Icons.Default.Settings, "Settings") { onSettings() }
        DrawerItem(Icons.Default.Info, "About Us") { onAboutUs() }
        DrawerItem(Icons.AutoMirrored.Filled.ExitToApp, "Log Out") { showLogoutDialog = true }
    }

    // Dialog wylogowania
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

fun UserResponse.toUser(): User {
    return User(
        id = this.id,
        name = this.name,
        surname = this.surname,
        email = this.email,
        imageUrl = this.imageUrl,
        premiumExpiration = this.premiumExpiration,
        status = Status.valueOf(this.status), // zakładając, że masz enum Status
        streak = this.streak
    )
}

