package study.snacktrackmobile.presentation.ui.views

import android.content.Context
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import study.snacktrackmobile.data.api.RecipeApi
import study.snacktrackmobile.data.api.TrainingApi
import study.snacktrackmobile.data.api.UserApi
import study.snacktrackmobile.data.model.User
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
import study.snacktrackmobile.data.services.AiApiService
import study.snacktrackmobile.data.database.AppDatabase // Odkomentuj i zaimportuj swoją bazę danych
import study.snacktrackmobile.data.model.dto.RecipeResponse
import kotlin.jvm.java
import androidx.activity.compose.BackHandler
import android.app.Activity
import android.content.ContextWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    navController: NavController,
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
    var recipeToOpen by remember { mutableStateOf<RecipeResponse?>(null) }
    var selectedProduct by remember { mutableStateOf<RegisteredAlimentationResponse?>(null) }
    val leftDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var authToken by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        authToken = TokenStorage.getToken(context)
        userViewModel.loadUserParameters(authToken ?: return@LaunchedEffect)
    }

    // ---------------------------------------------------------
    // KONFIGURACJA SHOPPING LIST VIEW MODEL (AI + Context)
    // ---------------------------------------------------------

    // 1. Pobranie instancji bazy danych (Jeśli używasz Room)
    // val db = remember { AppDatabase.getInstance(context) }

    // 2. Konfiguracja API dla AI
    val aiApiService = remember {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AiApiService::class.java)
    }

    // 3. Tworzenie ViewModelu z Fabryką
    // Zastąp `AppDatabase.getDatabase(context)` swoim sposobem pobierania bazy
    val shoppingListViewModel: ShoppingListViewModel = viewModel(
        factory = ShoppingListViewModelFactory(
            context = context,
            dao = study.snacktrackmobile.data.database.AppDatabase.getDatabase(context).shoppingListDao(),
            aiService = aiApiService
        )
    )
    // ---------------------------------------------------------


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
        "Recipes" -> true
        else -> false
    }

    val userResponse by profileViewModel.user.collectAsState()

    // OBLICZANIE STATUSU PREMIUM
    // Funkcja isPremiumActive pochodzi z pliku PremiumScreen.kt (ten sam pakiet)
    val isPremium = remember(userResponse) {
        isPremiumActive(userResponse?.premiumExpiration)
    }

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

    // POBRANIE AKTYWNOŚCI DO ZAMYKANIA APLIKACJI
    val activity = LocalContext.current

    // OBSŁUGA PRZYCISKU WSTECZ
    BackHandler(enabled = true) {
        when {
            // 1. Zamknij prawy panel powiadomień
            rightDrawerOpen -> rightDrawerOpen = false

            // 2. Zamknij lewe menu
            leftDrawerState.isOpen -> scope.launch { leftDrawerState.close() }

            // 3. Zamknij szczegóły przepisu
            selectedTab == "Recipes" && recipeToOpen != null -> {
                recipeToOpen = null
            }

            // 4. Zamknij edycję/dodawanie produktu
            selectedTab == "AddProduct" && selectedProduct != null -> {
                selectedProduct = null
                isEditMode = false
            }

            // 5. Wróć do ekranu głównego (Meals), jeśli jesteś gdzie indziej
            selectedTab != "Meals" -> {
                selectedTab = "Meals"
            }

            // 6. EXIT: Jesteś na głównym ekranie -> zamknij aplikację (zachowaj token)
            else -> {
                context.findActivity()?.finish()
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
                            // LOGIKA KLIKNIĘCIA
                            if (product.meal != null) {
                                // To jest PRZEPIS -> idziemy do zakładki Recipes i ustawiamy przepis do wyświetlenia
                                recipeToOpen = product.meal // <-- BEZPOŚREDNIE PRZYPISANIE
                                selectedTab = "Recipes"
                            } else {
                                // To jest ZWYKŁY PRODUKT -> idziemy do edycji produktu
                                selectedProduct = product
                                isEditMode = true
                                selectedTab = "AddProduct"
                            }
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
                        foodViewModel = foodViewModel,
                        registeredAlimentationViewModel = registeredAlimentationViewModel, // Przekazujemy VM
                        navController = navController,
                        selectedDate = selectedDate,
                        recipeToOpen = recipeToOpen,
                        onRecipeOpened = { recipeToOpen = null }// Przekazujemy wybraną datę z kalendarza
                    )

                    "Shopping" -> ShoppingListScreen(
                        viewModel = shoppingListViewModel,
                        selectedDate = selectedDate,
                        isUserPremium = isPremium, // <-- Przekazanie statusu Premium
                        onNavigateToPremium = {
                            selectedTab = "Premium" // <-- Przekierowanie do zakładki Premium
                        }
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
                                    selectedProduct = product
                                    isEditMode = false
                                },
                                // DODAJEMY OBSŁUGĘ Add Meal -> Przełącz na Recipes
                                onAddMealClick = {
                                    selectedTab = "Recipes"
                                    recipesViewModel.setScreen("Discover") // Lub "My recipes" zależnie co wolisz
                                    authToken?.let { token -> recipesViewModel.loadAllRecipes(token) }
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
                                isEditMode = selectedProduct!!.id > 0
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

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}