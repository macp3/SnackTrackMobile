package study.snacktrackmobile.ui.screens

import DropdownField
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import study.snacktrackmobile.data.api.* // Upewnij się, że importy modeli są poprawne
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.IngredientRequest
import study.snacktrackmobile.data.model.dto.RecipeRequest
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.repository.RegisteredAlimentationRepository
import study.snacktrackmobile.data.repository.UserRepository
import study.snacktrackmobile.presentation.ui.components.* // Tutaj zakładam, że masz DropdownField itp.
import study.snacktrackmobile.viewmodel.FoodViewModel
import study.snacktrackmobile.viewmodel.RecipeViewModel
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

enum class AddRecipeStep {
    FORM,
    SEARCH,
    DETAILS
}

@Composable
fun RecipesScreen(
    viewModel: RecipeViewModel,
    foodViewModel: FoodViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val registeredAlimentationViewModelInstance: RegisteredAlimentationViewModel by lazy {
        RegisteredAlimentationViewModel(
            RegisteredAlimentationRepository(Request.api)
        )
    }

    val userRepository: UserRepository by lazy {
        UserRepository() // zakładam, że masz Request.userApi
    }

    // Stany z ViewModel
    val selectedMode by viewModel.screen.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val favouriteIds by viewModel.favouriteIds.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    var token by remember { mutableStateOf("") }

    // Stan wyświetlania szczegółów przepisu (jeśli nie null -> pokazujemy ekran szczegółów)
    var selectedRecipeDetails by remember { mutableStateOf<RecipeResponse?>(null) }

    // --- STANY FORMULARZA DODAWANIA (ADD RECIPE) ---
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    val ingredientsForms = remember { mutableStateListOf<IngredientFormEntry>() }

    // Walidacja
    var isNameError by remember { mutableStateOf(false) }
    var nameErrorMessage by remember { mutableStateOf<String?>(null) }
    var isDescError by remember { mutableStateOf(false) }
    var descErrorMessage by remember { mutableStateOf<String?>(null) }
    var serverErrorMessage by remember { mutableStateOf<String?>(null) }

    // Nawigacja wewnątrz formularza
    var currentStep by remember { mutableStateOf(AddRecipeStep.FORM) }
    var activeIngredientIndex by remember { mutableStateOf<Int?>(null) }
    var tempSelectedProduct by remember { mutableStateOf<EssentialFoodResponse?>(null) }

    // Inicjalizacja: Pobranie tokena i danych
    LaunchedEffect(Unit) {
        TokenStorage.getToken(context)?.let { t ->
            token = t

            // Pobierz ID użytkownika
            val result = userRepository.getUserId(t)
            result.onSuccess { id ->
                viewModel.setCurrentUserId(id)
            }.onFailure { e ->
                Toast.makeText(context, "Failed to get user ID: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            // Domyślne ładowanie "My recipes"
            viewModel.loadMyRecipes(t)
        } ?: run {
            Toast.makeText(context, "No auth token found", Toast.LENGTH_SHORT).show()
        }
    }


    // Obsługa przycisku wstecz (BackHandler)
    // Priorytety: 1. Zamknij szczegóły przepisu -> 2. Wróć w krokach formularza -> 3. Zmień tryb na My Recipes
    BackHandler(enabled = (selectedRecipeDetails != null || (selectedMode == "Add recipe" && currentStep != AddRecipeStep.FORM))) {
        if (selectedRecipeDetails != null) {
            selectedRecipeDetails = null
        } else if (selectedMode == "Add recipe") {
            currentStep = AddRecipeStep.FORM
            tempSelectedProduct = null
        }
    }

    // 🔹 FUNKCJA WALIDACJI
    fun validateForm(): Boolean {
        isNameError = false; nameErrorMessage = null
        isDescError = false; descErrorMessage = null
        serverErrorMessage = null
        var isValid = true

        if (name.isBlank()) {
            isNameError = true; nameErrorMessage = "Name cannot be empty."
            isValid = false
        } else if (name.length > 50) {
            isNameError = true; nameErrorMessage = "Max 50 chars."
            isValid = false
        }

        if (desc.isBlank()) {
            isDescError = true; descErrorMessage = "Description cannot be empty."
            isValid = false
        } else if (desc.length > 100) {
            isDescError = true; descErrorMessage = "Max 100 chars."
            isValid = false
        }

        if (ingredientsForms.isEmpty()) {
            serverErrorMessage = "At least one ingredient required."
            isValid = false
        }
        return isValid
    }

    // 🔹 FUNKCJA SUBMIT
    fun submitRecipe() {
        if (!validateForm()) return

        val ingredientRequests = ingredientsForms.map { entry ->
            val defaultUnit: String? = entry.essentialFood?.servingSizeUnit ?: entry.essentialApi?.servingSizeUnit
            IngredientRequest(
                essentialFoodId = entry.essentialFood?.id,
                essentialApiId = entry.essentialApi?.id,
                amount = entry.amount,
                pieces = entry.pieces,
                defaultUnit = defaultUnit
            )
        }

        val recipeRequest = RecipeRequest(name = name, description = desc, ingredients = ingredientRequests)

        viewModel.addRecipe(
            token = token,
            request = recipeRequest,
            onSuccess = {
                Toast.makeText(context, "Recipe created!", Toast.LENGTH_SHORT).show()
                name = ""; desc = ""; ingredientsForms.clear()
                viewModel.setScreen("My recipes")
                currentStep = AddRecipeStep.FORM
            },
            onError = { error -> serverErrorMessage = error }
        )
    }

    // --- WIDOK GŁÓWNY ---

    // 1. Jeśli wybrano szczegóły przepisu, wyświetl tylko je
    if (selectedRecipeDetails != null) {
        RecipeDetailsScreen(
            recipe = selectedRecipeDetails!!,
            onBack = { selectedRecipeDetails = null }
        )
        return // Przerywamy rysowanie reszty ekranu
    }

    // 2. Standardowy widok
    Column(modifier = Modifier.fillMaxSize()) {

        // Pokaż Dropdown tylko jeśli nie jesteśmy w głębi formularza Add Recipe
        if (selectedMode != "Add recipe" || currentStep == AddRecipeStep.FORM) {
            DropdownField(
                label = "Mode",
                selected = selectedMode,
                options = listOf("My recipes", "Favourites", "Discover", "Add recipe"),
                onSelected = { mode ->
                    viewModel.setScreen(mode)
                    if (token.isNotEmpty()) {
                        when (mode) {
                            "My recipes" -> viewModel.loadMyRecipes(token)
                            "Favourites" -> viewModel.loadMyFavourites(token)
                            "Discover" -> viewModel.loadAllRecipes(token)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        // Zawartość w zależności od trybu
        when (selectedMode) {
            "My recipes", "Favourites", "Discover" -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recipes) { recipe ->
                        RecipeRow(
                            recipe = recipe,
                            isFavourite = favouriteIds.contains(recipe.id),
                            // Sprawdzamy czy currentUserId nie jest nullem i czy pasuje do autora
                            isAuthor = (currentUserId != null && recipe.authorId == currentUserId),
                            onClick = { selectedRecipeDetails = recipe },
                            onToggleFavourite = { viewModel.toggleFavourite(token, recipe) },
                            onDelete = { viewModel.deleteRecipe(token, recipe.id) }
                        )
                    }
                }
            }

            "Add recipe" -> {
                when (currentStep) {
                    AddRecipeStep.FORM -> {
                        AddRecipeForm(
                            name = name,
                            desc = desc,
                            ingredients = ingredientsForms,
                            isNameError = isNameError,
                            nameErrorMessage = nameErrorMessage,
                            isDescError = isDescError,
                            descErrorMessage = descErrorMessage,
                            serverErrorMessage = serverErrorMessage,
                            onNameChange = { name = it; isNameError = false },
                            onDescChange = { desc = it; isDescError = false },
                            onStartAddIngredient = {
                                serverErrorMessage = null
                                activeIngredientIndex = ingredientsForms.size
                                currentStep = AddRecipeStep.SEARCH
                            },
                            onSelectIngredient = { idx ->
                                serverErrorMessage = null
                                activeIngredientIndex = idx
                                tempSelectedProduct = ingredientsForms[idx].essentialFood
                                currentStep = AddRecipeStep.DETAILS
                            },
                            onSubmit = ::submitRecipe
                        )
                    }
                    AddRecipeStep.SEARCH -> {
                        AddProductScreen(
                            selectedDate = "",
                            selectedMeal = "Ingredient",
                            navController = navController,
                            foodViewModel = foodViewModel,
                            isRecipeMode = true,
                            onProductClick = { registeredAlimentation ->
                                tempSelectedProduct = registeredAlimentation.essentialFood
                                currentStep = AddRecipeStep.DETAILS
                            }
                        )
                    }
                    AddRecipeStep.DETAILS -> {
                        val product = tempSelectedProduct
                        if (product != null) {
                            // Dummy obiekt do DetailsScreen
                            val dummyAlimentation = RegisteredAlimentationResponse(
                                id = 0, userId = 0, essentialFood = product,
                                mealApi = null, meal = null, timestamp = "",
                                amount = 0f, pieces = 0f, mealName = ""
                            )

                            ProductDetailsScreen(
                                alimentation = dummyAlimentation,
                                selectedDate = "",
                                selectedMeal = "",
                                // Prowizoryczna instancja VM lub przekaż właściwą
                                registeredAlimentationViewModel = registeredAlimentationViewModelInstance,
                                onBack = {
                                    currentStep = AddRecipeStep.FORM
                                    tempSelectedProduct = null
                                },
                                onYieldResult = { amount, pieces ->
                                    activeIngredientIndex?.let { idx ->
                                        val newEntry = IngredientFormEntry(
                                            essentialFood = product,
                                            amount = amount,
                                            pieces = pieces
                                        )
                                        if (idx == ingredientsForms.size) ingredientsForms.add(newEntry)
                                        else ingredientsForms[idx] = newEntry
                                    }
                                    tempSelectedProduct = null
                                    activeIngredientIndex = null
                                    currentStep = AddRecipeStep.FORM
                                }
                            )
                        } else {
                            // Fallback
                            LaunchedEffect(Unit) { currentStep = AddRecipeStep.FORM }
                        }
                    }
                }
            }
        }
    }
}

// --- KOMPONENT: WIERSZ PRZEPISU ---
@Composable
fun RecipeRow(
    recipe: RecipeResponse,
    isFavourite: Boolean,
    isAuthor: Boolean,
    onClick: () -> Unit,
    onToggleFavourite: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // Cały wiersz klikalny
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEWA STRONA: Tekst
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = if (recipe.description.isNullOrBlank()) "No description" else recipe.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // PRAWA STRONA: Ikony (Serce + X)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Serduszko
                IconButton(onClick = onToggleFavourite) {
                    Icon(
                        imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favourite",
                        tint = if (isFavourite) Color.Red else Color.Gray
                    )
                }

                // X - tylko jeśli user jest autorem
                if (isAuthor) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
}