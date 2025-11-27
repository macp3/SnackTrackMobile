package study.snacktrackmobile.presentation.ui.views

import android.content.Context
import android.app.Activity
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
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
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.model.dto.UserResponse
import study.snacktrackmobile.data.model.enums.Status
import study.snacktrackmobile.data.network.ApiConfig
import study.snacktrackmobile.data.repository.NotificationsRepository
import study.snacktrackmobile.data.repository.RecipeRepository
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.components.*
import study.snacktrackmobile.viewmodel.*
import study.snacktrackmobile.data.services.AiApiService
import study.snacktrackmobile.data.database.AppDatabase
import study.snacktrackmobile.data.api.FoodApi
import study.snacktrackmobile.data.repository.RegisteredAlimentationRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    navController: NavController,
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
    var recipeToOpen by remember { mutableStateOf<RecipeResponse?>(null) }
    var selectedProduct by remember { mutableStateOf<RegisteredAlimentationResponse?>(null) }
    val leftDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 🔹 NOWY STAN: Czy RecipesScreen wyświetla detale?
    var isRecipeDetailsVisible by remember { mutableStateOf(false) }

    var authToken by remember { mutableStateOf<String?>(null) }

    val jsonConfig = remember {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            coerceInputValues = true
        }
    }

    val userApi = remember {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApi::class.java)
    }
    val profileViewModel = remember { ProfileViewModel(userApi) }
    val userResponse by profileViewModel.user.collectAsState()
    val isUnauthorized by profileViewModel.unauthorized.collectAsState()

    LaunchedEffect(Unit) {
        authToken = TokenStorage.getToken(context)
    }

    LaunchedEffect(authToken) {
        authToken?.let { token ->
            profileViewModel.loadProfile(token)
            profileViewModel.getBodyParameters(token)
        }
    }

    LaunchedEffect(isUnauthorized) {
        if (isUnauthorized) {
            TokenStorage.clearToken(context)
            navController.navigate("StartView") {
                popUpTo("MainView") { inclusive = true }
            }
        }
    }

    LaunchedEffect(selectedDate) {
        if (selectedTab == "AddProduct" || selectedProduct != null) {
            selectedProduct = null
            isEditMode = false
            selectedTab = "Meals"
        }
    }

    if (authToken == null || userResponse == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF2E7D32))
        }
        return
    }

    // --- KONFIGURACJA API ---
    val aiApiService = remember {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AiApiService::class.java)
    }

    val foodApi = remember {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(jsonConfig.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FoodApi::class.java)
    }

    val shoppingListViewModel: ShoppingListViewModel = viewModel(
        factory = ShoppingListViewModelFactory(
            context = context,
            dao = AppDatabase.getDatabase(context).shoppingListDao(),
            aiService = aiApiService
        )
    )

    val trainingApi = remember {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TrainingApi::class.java)
    }
    val trainingViewModel = remember { TrainingViewModel(trainingApi) }

    val recipeApi = remember {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(jsonConfig.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(RecipeApi::class.java)
    }
    val recipeRepository = remember { RecipeRepository(recipeApi) }
    val recipesViewModel: RecipeViewModel = viewModel(
        factory = RecipeViewModel.provideFactory(recipeRepository)
    )

    val currentRecipeScreen by recipesViewModel.screen.collectAsState()

    val commentViewModel: CommentViewModel = viewModel(
        factory = CommentViewModel.provideFactory()
    )

    val regApi = remember { study.snacktrackmobile.data.api.Request.api }
    val regRepo = remember { RegisteredAlimentationRepository(regApi) }

    val registeredAlimentationViewModel: RegisteredAlimentationViewModel = viewModel(
        factory = RegisteredAlimentationViewModel.provideFactory(
            repository = regRepo,
            foodApi = foodApi,
            aiService = aiApiService
        )
    )

    val userTraining by remember { derivedStateOf { trainingViewModel.userTraining } }

    // 🔹 ZMIANA: showCalendar zależy teraz od flagi isRecipeDetailsVisible dla Recipes
    val showCalendar = when (selectedTab) {
        "Meals" -> true
        "Shopping" -> true
        "Training" -> userTraining != null
        "Recipes" -> isRecipeDetailsVisible // Pokaż kalendarz tylko gdy wyświetlamy detale
        else -> false
    }

    val isPremium = remember(userResponse) { isPremiumActive(userResponse?.premiumExpiration) }

    LaunchedEffect(userResponse) {
        if (userResponse != null) {
            recipesViewModel.setCurrentUserId(userResponse!!.id)
            commentViewModel.setCurrentUserId(userResponse!!.id)
        }
    }

    LaunchedEffect(loggedUserEmail) {
        shoppingListViewModel.setUser(loggedUserEmail)
        if (selectedTab == "Shopping") shoppingListViewModel.setDate(selectedDate)
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == "Recipes") {
            authToken?.let { token -> recipesViewModel.loadAllRecipes(token) }
        }
    }

    // Obsługa przycisku Wstecz
    BackHandler(enabled = true) {
        when {
            rightDrawerOpen -> rightDrawerOpen = false
            leftDrawerState.isOpen -> scope.launch { leftDrawerState.close() }

            // Jeśli otwarty jest szczegół przepisu -> zamknij go
            selectedTab == "Recipes" && recipeToOpen != null -> recipeToOpen = null

            // Jeśli jesteśmy w trybie dodawania/edycji przepisu -> wracamy do listy
            selectedTab == "Recipes" && (currentRecipeScreen == "Add recipe_INTERNAL") -> {
                recipesViewModel.setScreen("My recipes")
            }

            // Jeśli jesteśmy w detalach (ale nie z recipeToOpen), musimy obsłużyć to w RecipesScreen
            // (Tam jest własny BackHandler, ten tutaj jest globalny)

            selectedTab == "AddProduct" && selectedProduct != null -> {
                selectedProduct = null
                isEditMode = false
            }
            selectedTab != "Meals" -> selectedTab = "Meals"
            else -> context.findActivity()?.finish()
        }
    }

    ModalNavigationDrawer(
        drawerState = leftDrawerState,
        gesturesEnabled = !rightDrawerOpen,
        drawerContent = {
            DrawerContent(
                onClose = { scope.launch { leftDrawerState.close() } },
                onNavigate = { selectedTab = it },
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
                BottomNavigationBar(
                    selectedItem = selectedTab,
                    onItemSelected = { tab ->
                        selectedTab = tab
                        if (tab == "Shopping") shoppingListViewModel.setDate(selectedDate)
                    }
                )
            },
            modifier = Modifier.fillMaxSize().background(Color.White)
        ) { paddingValues ->

            // Główny Box kontenera
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                // WARSTWA 1: TREŚĆ (Listy itp.)
                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedTab) {
                        "Meals" -> MealsDailyView(
                            selectedDate = selectedDate,
                            viewModel = registeredAlimentationViewModel,
                            navController = navController,
                            isUserPremium = isPremium,
                            onNavigateToPremium = { selectedTab = "Premium" },
                            onEditProduct = { product ->
                                if (product.meal != null) {
                                    recipeToOpen = product.meal
                                    selectedTab = "Recipes"
                                } else {
                                    selectedProduct = product
                                    isEditMode = true
                                    selectedTab = "AddProduct"
                                }
                            },
                            onAddProductClick = { mealName ->
                                selectedMeal = mealName
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
                            foodViewModel = foodViewModel,
                            navController = navController,
                            registeredAlimentationViewModel = registeredAlimentationViewModel,
                            commentViewModel = commentViewModel,
                            selectedDate = selectedDate,
                            recipeToOpen = recipeToOpen,
                            onRecipeOpened = { recipeToOpen = null },
                            // 🔹 NOWY CALLBACK: Informujemy MainView czy wyświetlamy detale
                            onDetailsVisibilityChange = { isVisible ->
                                isRecipeDetailsVisible = isVisible
                            }
                        )
                        "Shopping" -> ShoppingListScreen(
                            viewModel = shoppingListViewModel,
                            selectedDate = selectedDate,
                            isUserPremium = isPremium,
                            onNavigateToPremium = { selectedTab = "Premium" }
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
                                    onAddMealClick = {
                                        selectedTab = "Recipes"
                                        recipesViewModel.setScreen("Discover")
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
                                        selectedTab = "Meals"
                                    },
                                    registeredAlimentationViewModel = registeredAlimentationViewModel,
                                    isEditMode = isEditMode // Przekazujemy czy to tryb edycji
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
                                authToken?.let { token -> profileViewModel.updatePremium(token, newDate) }
                            },
                            onExtendPremium = { newDate ->
                                authToken?.let { token -> profileViewModel.updatePremium(token, newDate) }
                            }
                        )
                        "AboutUs" -> AboutUsScreen(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // WARSTWA 2: PŁYWAJĄCY PASEK SUMMARY BAR
                if (selectedTab != "AddProduct" &&
                    selectedTab != "AddProductToDatabase" &&
                    selectedTab != "AboutUs" &&
                    selectedTab != "Profile" &&
                    selectedTab != "EditProfile" &&
                    selectedTab != "Premium" &&
                    selectedTab != "Recipes") {

                    SummaryBar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
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
        status = Status.valueOf(this.status),
        streak = this.streak
    )
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}