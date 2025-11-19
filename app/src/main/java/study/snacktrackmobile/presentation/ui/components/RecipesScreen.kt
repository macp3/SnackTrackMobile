package study.snacktrackmobile.presentation.ui.components

import DropdownField
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import study.snacktrackmobile.data.api.Request
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.IngredientRequest
import study.snacktrackmobile.data.model.dto.RecipeRequest
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.repository.RegisteredAlimentationRepository
import study.snacktrackmobile.data.repository.UserRepository
import study.snacktrackmobile.presentation.ui.components.*
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
        RegisteredAlimentationViewModel(RegisteredAlimentationRepository(Request.api))
    }
    val userRepository: UserRepository by lazy { UserRepository() }

    val selectedMode by viewModel.screen.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val favouriteIds by viewModel.favouriteIds.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }

    var token by remember { mutableStateOf("") }

    // UI States
    var selectedRecipeDetails by remember { mutableStateOf<RecipeResponse?>(null) }

    // Form States
    var editingRecipeId by remember { mutableStateOf<Int?>(null) }

    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    val ingredientsForms = remember { mutableStateListOf<IngredientFormEntry>() }

    // Validation
    var isNameError by remember { mutableStateOf(false) }
    var nameErrorMessage by remember { mutableStateOf<String?>(null) }
    var isDescError by remember { mutableStateOf(false) }
    var descErrorMessage by remember { mutableStateOf<String?>(null) }
    var serverErrorMessage by remember { mutableStateOf<String?>(null) }

    var currentStep by remember { mutableStateOf(AddRecipeStep.FORM) }
    var activeIngredientIndex by remember { mutableStateOf<Int?>(null) }
    var tempSelectedProduct by remember { mutableStateOf<EssentialFoodResponse?>(null) }

    LaunchedEffect(Unit) {
        TokenStorage.getToken(context)?.let { t ->
            token = t
            userRepository.getUserId(t).onSuccess { id -> viewModel.setCurrentUserId(id) }
            viewModel.loadMyRecipes(t)
        }
    }

    fun uploadImageIfSelected(recipeId: Int) {
        if (selectedImageUri != null) {
            viewModel.uploadRecipeImage(
                context = context,
                token = token,
                recipeId = recipeId,
                imageUri = selectedImageUri!!,
                onSuccess = {
                    Toast.makeText(context, "Recipe & Image saved!", Toast.LENGTH_SHORT).show()
                    // Czyścimy formularz dopiero jak zdjęcie się uda (lub można wcześniej, zależy od preferencji)
                },
                onError = { error ->
                    Toast.makeText(context, "Recipe saved, but image failed: $error", Toast.LENGTH_LONG).show()
                }
            )
        } else {
            Toast.makeText(context, "Recipe saved!", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearForm() {
        name = ""; desc = ""; ingredientsForms.clear()
        selectedImageUri = null
        existingImageUrl = null
        editingRecipeId = null
        isNameError = false; isDescError = false
    }

    BackHandler(enabled = (selectedRecipeDetails != null || selectedMode == "Add recipe")) {
        if (selectedRecipeDetails != null) {
            selectedRecipeDetails = null
        } else if (selectedMode == "Add recipe") {
            if (currentStep != AddRecipeStep.FORM) {
                currentStep = AddRecipeStep.FORM
            } else {
                clearForm()
                viewModel.setScreen("My recipes")
                viewModel.loadMyRecipes(token)
            }
        }
    }

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

    // 🔹 NAPRAWIONA FUNKCJA SUBMIT 🔹
    fun submitRecipe() {
        if (!validateForm()) return

        val ingredientRequests = ingredientsForms.map { /* mapowanie bez zmian */
            IngredientRequest(it.essentialFood?.id, it.essentialApi?.id, it.amount, it.pieces, it.essentialFood?.servingSizeUnit)
        }

        val request = RecipeRequest(name = name, description = desc, ingredients = ingredientRequests)

        if (editingRecipeId == null) {
            // --- TWORZENIE NOWEGO (ADD) ---
            viewModel.addRecipe(
                token = token,
                request = request,
                onSuccess = { newId -> // 👈 Teraz dostajemy tutaj ID!
                    // Mamy ID, więc możemy od razu wgrać zdjęcie
                    uploadImageIfSelected(newId)

                    clearForm()
                    viewModel.setScreen("My recipes")
                },
                onError = { serverErrorMessage = it }
            )
        } else {
            // --- EDYCJA ISTNIEJĄCEGO (UPDATE) ---
            viewModel.updateRecipe(
                token = token,
                id = editingRecipeId!!,
                request = request,
                onSuccess = {
                    // Znamy ID (editingRecipeId), wgrywamy zdjęcie
                    uploadImageIfSelected(editingRecipeId!!)

                    clearForm()
                    viewModel.setScreen("My recipes")
                },
                onError = { serverErrorMessage = it }
            )
        }
    }

    fun startEditing(recipe: RecipeResponse) {
        selectedRecipeDetails = null
        clearForm()

        editingRecipeId = recipe.id
        name = recipe.name
        desc = recipe.description ?: ""
        imageUrl = recipe.imageUrl ?: ""

        recipe.ingredients.forEach { ing ->
            val entry = IngredientFormEntry(
                essentialFood = ing.essentialFood,
                essentialApi = ing.essentialApi,
                amount = ing.amount,
                pieces = ing.pieces
            )
            ingredientsForms.add(entry)
        }

        viewModel.setScreen("Add recipe")
        currentStep = AddRecipeStep.FORM
    }

    // Views
    if (selectedRecipeDetails != null) {
        val r = selectedRecipeDetails!!
        RecipeDetailsScreen(
            recipe = r,
            isAuthor = (currentUserId != null && r.authorId == currentUserId),
            isFavourite = favouriteIds.contains(r.id),
            onBack = { selectedRecipeDetails = null },
            onEdit = { startEditing(r) },
            onDelete = {
                viewModel.deleteRecipe(token, r.id)
                selectedRecipeDetails = null
                Toast.makeText(context, "Recipe deleted", Toast.LENGTH_SHORT).show()
            },
            onToggleFavourite = { viewModel.toggleFavourite(token, r) }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (selectedMode != "Add recipe") {
            DropdownField(
                label = "Mode",
                selected = selectedMode,
                options = listOf("My recipes", "Favourites", "Discover", "Add recipe"),
                onSelected = { mode ->
                    if (mode == "Add recipe") clearForm()
                    viewModel.setScreen(mode)
                    if (token.isNotEmpty()) {
                        when (mode) {
                            "My recipes" -> viewModel.loadMyRecipes(token)
                            "Favourites" -> viewModel.loadMyFavourites(token)
                            "Discover" -> viewModel.loadAllRecipes(token)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
        } else if (currentStep == AddRecipeStep.FORM) {
            Text(
                text = if(editingRecipeId != null) "Edit Recipe" else "New Recipe",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold
            )
        }

        when (selectedMode) {
            "My recipes", "Favourites", "Discover" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recipes) { recipe ->
                        RecipeRow(
                            recipe = recipe,
                            isFavourite = favouriteIds.contains(recipe.id),
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
                            // 1. Przekazujemy URL z backendu (użyj zmiennej existingImageUrl, którą zdefiniowaliśmy wcześniej)
                            // Jeśli w RecipesScreen masz zmienną o nazwie 'imageUrl', zmień ją na tę zmienną.
                            // W poprzednim kroku sugerowałem nazwę 'existingImageUrl' dla URL z backendu.
                            imageUrl = existingImageUrl,

                            // 2. Przekazujemy URI wybrane z galerii (to brakowało)
                            selectedImageUri = selectedImageUri,

                            ingredients = ingredientsForms,
                            isNameError = isNameError,
                            nameErrorMessage = nameErrorMessage,
                            isDescError = isDescError,
                            descErrorMessage = descErrorMessage,
                            serverErrorMessage = serverErrorMessage,

                            onNameChange = { name = it; isNameError = false },
                            onDescChange = { desc = it; isDescError = false },

                            // 3. Obsługa wyboru zdjęcia (to brakowało)
                            onImageSelected = { uri -> selectedImageUri = uri },

                            // 4. USUNIĘTE: onImageUrlChange = ... (już nie wpisujemy ręcznie URL)

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
                            val dummyAlimentation = RegisteredAlimentationResponse(
                                id = 0, userId = 0, essentialFood = product,
                                mealApi = null, meal = null, timestamp = "",
                                amount = 0f, pieces = 0f, mealName = ""
                            )
                            ProductDetailsScreen(
                                alimentation = dummyAlimentation,
                                selectedDate = "",
                                selectedMeal = "",
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
                            LaunchedEffect(Unit) { currentStep = AddRecipeStep.FORM }
                        }
                    }
                }
            }
        }
    }
}
