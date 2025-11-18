package study.snacktrackmobile.presentation.ui.components

import DropdownField
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.IngredientRequest
import study.snacktrackmobile.data.model.dto.RecipeRequest
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.repository.RegisteredAlimentationRepository
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.viewmodel.FoodViewModel
import study.snacktrackmobile.viewmodel.RecipeViewModel
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

// Enum kroków
enum class AddRecipeStep {
    FORM,
    SEARCH,
    DETAILS
}

@Composable
fun RecipesScreen(
    recipeViewModel: RecipeViewModel,
    foodViewModel: FoodViewModel,
    navController: NavController,
    onRecipeClick: (Int) -> Unit
) {
    val context = LocalContext.current

    // Stan widoku
    var selected by remember { mutableStateOf("My recipes") }

    // Pobieramy dane z ViewModelu
    val recipes by recipeViewModel.recipes.collectAsState()
    val favouritesIds by recipeViewModel.favouritesIds.collectAsState()

    var token by remember { mutableStateOf("") }

    val registeredAlimentationViewModel: RegisteredAlimentationViewModel = viewModel(
        factory = RegisteredAlimentationViewModel.provideFactory(
            RegisteredAlimentationRepository(study.snacktrackmobile.data.api.Request.api)
        )
    )

    // Pobranie tokenu przy starcie
    LaunchedEffect(Unit) {
        TokenStorage.getToken(context)?.let { t ->
            token = t
            recipeViewModel.loadMyRecipes(t)
        } ?: run {
            Toast.makeText(context, "No auth token found", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Stan formularza "Add Recipe" ---
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    // Używamy mutableStateListOf, co zwraca SnapshotStateList
    val ingredientsForms = remember { mutableStateListOf<IngredientFormEntry>() }

    // Walidacja
    var isNameError by remember { mutableStateOf(false) }
    var nameErrorMessage by remember { mutableStateOf<String?>(null) }
    var isDescError by remember { mutableStateOf(false) }
    var descErrorMessage by remember { mutableStateOf<String?>(null) }
    var serverErrorMessage by remember { mutableStateOf<String?>(null) }

    // Nawigacja wewnętrzna
    var currentStep by remember { mutableStateOf(AddRecipeStep.FORM) }
    var activeIngredientIndex by remember { mutableStateOf<Int?>(null) }
    var tempSelectedProduct by remember { mutableStateOf<EssentialFoodResponse?>(null) }

    // Back Handler
    BackHandler(enabled = (selected == "Add recipe" && currentStep != AddRecipeStep.FORM)) {
        currentStep = AddRecipeStep.FORM
        tempSelectedProduct = null
        activeIngredientIndex = null
    }

    // Funkcje pomocnicze
    fun validateForm(): Boolean {
        isNameError = false; nameErrorMessage = null
        isDescError = false; descErrorMessage = null
        serverErrorMessage = null
        var isValid = true

        if (name.isBlank()) {
            isNameError = true; nameErrorMessage = "Recipe name cannot be empty."; isValid = false
        } else if (name.length > 50) {
            isNameError = true; nameErrorMessage = "Recipe name must be max 50 characters."; isValid = false
        }

        if (desc.isBlank()) {
            isDescError = true; descErrorMessage = "Description cannot be empty."; isValid = false
        } else if (desc.length > 100) {
            isDescError = true; descErrorMessage = "Description must be max 100 characters."; isValid = false
        }

        if (ingredientsForms.isEmpty()) {
            serverErrorMessage = "The meal has to have at least one ingredient."
            isValid = false
        }
        return isValid
    }

    fun submitRecipe() {
        if (!validateForm()) return

        val ingredientRequests = ingredientsForms.map { entry ->
            val defaultUnit: String? = entry.essentialFood?.servingSizeUnit ?: entry.essentialApi?.servingSizeUnit

            // Bezpieczne rzutowanie na float
            val safeAmount = entry.amount ?: 0f
            val safePieces = entry.pieces ?: 0f

            IngredientRequest(
                essentialFoodId = entry.essentialFood?.id,
                essentialApiId = entry.essentialApi?.id,
                amount = safeAmount,
                pieces = safePieces,
                defaultUnit = defaultUnit
            )
        }

        val recipeRequest = RecipeRequest(name = name, description = desc, ingredients = ingredientRequests)

        recipeViewModel.addRecipe(
            token = token,
            request = recipeRequest,
            onSuccess = {
                Toast.makeText(context, "Recipe created successfully!", Toast.LENGTH_LONG).show()
                name = ""; desc = ""; ingredientsForms.clear()
                selected = "My recipes"
                recipeViewModel.loadMyRecipes(token)
                currentStep = AddRecipeStep.FORM
            },
            onError = { error ->
                serverErrorMessage = error
                Log.e("API_ERROR", "Recipe creation failed: $error")
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Selector Trybu
        DropdownField( // Używamy teraz Twojej zaimportowanej, poprawnej wersji
            label = "Mode",
            selected = selected,
            options = listOf("My recipes", "Favourites", "Discover", "Add recipe"),
            onSelected = { mode ->
                selected = mode
                when (mode) {
                    "My recipes" ->recipeViewModel.loadMyRecipes(token)
                    "Favourites" -> recipeViewModel.loadFavouriteRecipes(token)
                    "Discover" -> recipeViewModel.loadAllRecipes(token)
                    "Add recipe" -> {
                        currentStep = AddRecipeStep.FORM
                        serverErrorMessage = null
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (selected) {
            "My recipes", "Favourites", "Discover" -> {
                RecipeListDisplay(
                    items = recipes,
                    favourites = favouritesIds,
                    inMyRecipes = selected == "My recipes",
                    onClick = onRecipeClick,
                    onDelete = { id -> recipeViewModel.deleteRecipe(token, id) },
                    onEdit = { recipe -> if (selected == "My recipes") onRecipeClick(recipe.id) },
                    onFavouriteToggle = { id ->
                        recipeViewModel.toggleFavourite(token, id)
                    }
                )
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
                            onNameChange = {
                                name = it; isNameError = false; nameErrorMessage = null
                            },
                            onDescChange = {
                                desc = it; isDescError = false; descErrorMessage = null
                            },
                            onStartAddIngredient = {
                                serverErrorMessage = null
                                activeIngredientIndex = ingredientsForms.size
                                currentStep = AddRecipeStep.SEARCH
                            },
                            onSelectIngredient = { index ->
                                // Edycja istniejącego składnika
                                if (index in ingredientsForms.indices) {
                                    activeIngredientIndex = index
                                    tempSelectedProduct = ingredientsForms[index].essentialFood
                                    currentStep = AddRecipeStep.DETAILS
                                }
                            },
                            onSubmit = { submitRecipe() }
                        )
                    }

                    AddRecipeStep.SEARCH -> {
                        AddProductScreen(
                            selectedDate = "",
                            selectedMeal = "Select Ingredient",
                            navController = navController,
                            foodViewModel = foodViewModel,
                            isRecipeMode = true,
                            onProductClick = { registeredAlimentation ->
                                tempSelectedProduct = registeredAlimentation.essentialFood
                                currentStep = AddRecipeStep.DETAILS
                            },
                        )
                    }

                    AddRecipeStep.DETAILS -> {
                        val product = tempSelectedProduct
                        if (product != null) {
                            val dummyAlimentation = RegisteredAlimentationResponse(
                                id = 0, userId = 0, essentialFood = product,
                                mealApi = null, meal = null, timestamp = "",
                                amount = 0f, pieces = 0f, mealName = ""
                            )

                            ProductDetailsScreen(
                                alimentation = dummyAlimentation,
                                selectedDate = "",
                                selectedMeal = "",
                                registeredAlimentationViewModel = registeredAlimentationViewModel,
                                onBack = {
                                    currentStep = AddRecipeStep.FORM
                                    tempSelectedProduct = null
                                    activeIngredientIndex = null
                                },
                                onYieldResult = { amount, pieces ->
                                    activeIngredientIndex?.let { idx ->
                                        val newEntry = IngredientFormEntry(
                                            essentialFood = product,
                                            essentialApi = null,
                                            amount = amount,
                                            pieces = pieces
                                        )

                                        if (idx == ingredientsForms.size) {
                                            ingredientsForms.add(newEntry)
                                        } else if (idx in 0 until ingredientsForms.size) {
                                            ingredientsForms[idx] = newEntry
                                        }
                                    }
                                    tempSelectedProduct = null
                                    activeIngredientIndex = null
                                    currentStep = AddRecipeStep.FORM
                                }
                            )
                        } else {
                            LaunchedEffect(Unit) { currentStep = AddRecipeStep.FORM }
                        }
                    }
                }
            }
        }
    }
}