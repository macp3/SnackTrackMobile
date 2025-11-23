package study.snacktrackmobile.presentation.ui.components

import DropdownField
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.cast.framework.media.ImagePicker
import study.snacktrackmobile.data.model.dto.ApiFoodResponseDetailed
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.IngredientRequest
import study.snacktrackmobile.data.model.dto.RecipeRequest
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.repository.UserRepository
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.FoodViewModel
import study.snacktrackmobile.viewmodel.RecipeViewModel
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
    navController: NavController,
    registeredAlimentationViewModel: RegisteredAlimentationViewModel,
    selectedDate: String? = null,
    recipeToOpen: RecipeResponse? = null,
    onRecipeOpened: () -> Unit = {}
) {
    val context = LocalContext.current
    val userRepository: UserRepository by lazy { UserRepository() }
    val selectedMode by viewModel.screen.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val favouriteIds by viewModel.favouriteIds.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedRecipeDetails by remember { mutableStateOf<RecipeResponse?>(null) }

    var token by remember { mutableStateOf("") }

    // --- Form States ---
    var editingRecipeId by remember { mutableStateOf<Int?>(null) }
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    val ingredientsForms = remember { mutableStateListOf<IngredientFormEntry>() }

    // --- Validation ---
    var isNameError by remember { mutableStateOf(false) }
    var nameErrorMessage by remember { mutableStateOf<String?>(null) }
    var isDescError by remember { mutableStateOf(false) }
    var descErrorMessage by remember { mutableStateOf<String?>(null) }
    var serverErrorMessage by remember { mutableStateOf<String?>(null) }

    // --- Steps Logic ---
    var currentStep by remember { mutableStateOf(AddRecipeStep.FORM) }
    var activeIngredientIndex by remember { mutableStateOf<Int?>(null) }

    // 🔹 Przechowujemy cały wrapper, żeby obsłużyć API i Local DB
    var tempSelectedProduct by remember { mutableStateOf<RegisteredAlimentationResponse?>(null) }

    LaunchedEffect(Unit) {
        TokenStorage.getToken(context)?.let { t ->
            token = t
            userRepository.getUserId(t).onSuccess { id -> viewModel.setCurrentUserId(id) }
            viewModel.loadMyRecipes(t)
        }
    }
    LaunchedEffect(recipeToOpen) {
        if (recipeToOpen != null) {
            selectedRecipeDetails = recipeToOpen
            onRecipeOpened()
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

    fun submitRecipe() {
        if (!validateForm()) return

        // 🔹 MAPOWANIE: Dostosowane do Twojego DTO IngredientRequest
        val ingredientRequests = ingredientsForms.map { form ->
            IngredientRequest(
                essentialFoodId = form.essentialFood?.id,
                // Tu była zmiana: mapujemy essentialApi.id na essentialApiId
                essentialApiId = form.essentialApi?.id,
                amount = form.amount,
                pieces = form.pieces,
                // Tu była zmiana: defaultUnit zamiast servingUnit
                defaultUnit = form.essentialFood?.servingSizeUnit ?: form.essentialApi?.servingSizeUnit
            )
        }

        val request = RecipeRequest(name = name, description = desc, ingredients = ingredientRequests)

        if (editingRecipeId == null) {
            // ADD
            viewModel.addRecipe(
                token = token,
                request = request,
                onSuccess = { newId ->
                    uploadImageIfSelected(newId)
                    clearForm()
                    viewModel.setScreen("My recipes")
                },
                onError = { serverErrorMessage = it }
            )
        } else {
            // UPDATE
            viewModel.updateRecipe(
                token = token,
                id = editingRecipeId!!,
                request = request,
                onSuccess = {
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
        existingImageUrl = recipe.imageUrl

        recipe.ingredients.forEach { ing ->
            val entry = IngredientFormEntry(
                essentialFood = ing.essentialFood,
                essentialApi = ing.essentialApi, // 🔹 Wczytujemy API source
                amount = ing.amount,
                pieces = ing.pieces
            )
            ingredientsForms.add(entry)
        }

        viewModel.setScreen("Add recipe")
        currentStep = AddRecipeStep.FORM
    }

    // --- UI RENDER ---

    if (selectedRecipeDetails != null) {
        val r = selectedRecipeDetails!!
        RecipeDetailsScreen(
            recipe = r,
            isAuthor = (currentUserId != null && r.authorId == currentUserId),
            isFavourite = favouriteIds.contains(r.id),
            selectedDate = selectedDate ?: "",
            registeredAlimentationViewModel = registeredAlimentationViewModel,
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
                RecipeListDisplay(
                    items = recipes,
                    favouriteIds = favouriteIds,
                    currentUserId = currentUserId,
                    onClick = { selectedRecipeDetails = it },
                    onToggleFavourite = { viewModel.toggleFavourite(token, it) },
                    onDelete = { viewModel.deleteRecipe(token, it) }
                )
            }

            "Add recipe" -> {
                when (currentStep) {
                    AddRecipeStep.FORM -> {
                        AddRecipeForm(
                            name = name,
                            desc = desc,
                            imageUrl = existingImageUrl,
                            selectedImageUri = selectedImageUri,
                            ingredients = ingredientsForms,
                            isNameError = isNameError,
                            nameErrorMessage = nameErrorMessage,
                            isDescError = isDescError,
                            descErrorMessage = descErrorMessage,
                            serverErrorMessage = serverErrorMessage,
                            onNameChange = { name = it; isNameError = false },
                            onDescChange = { desc = it; isDescError = false },
                            onImageSelected = { uri -> selectedImageUri = uri },
                            onStartAddIngredient = {
                                serverErrorMessage = null
                                activeIngredientIndex = ingredientsForms.size
                                currentStep = AddRecipeStep.SEARCH
                            },
                            onSelectIngredient = { idx ->
                                serverErrorMessage = null
                                activeIngredientIndex = idx

                                // Odtwarzamy obiekt wrapper do edycji
                                val formEntry = ingredientsForms[idx]
                                tempSelectedProduct = RegisteredAlimentationResponse(
                                    id = 0, userId = 0,
                                    essentialFood = formEntry.essentialFood,
                                    mealApi = formEntry.essentialApi, // 🔹 Zachowujemy API
                                    meal = null, timestamp = "", amount = 0f, pieces = 0f, mealName = ""
                                )
                                currentStep = AddRecipeStep.DETAILS
                            },
                            onDeleteIngredient = { idx ->
                                ingredientsForms.removeAt(idx)
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
                                // 🔹 Zapisujemy CAŁY wrapper
                                tempSelectedProduct = registeredAlimentation
                                currentStep = AddRecipeStep.DETAILS
                            }
                        )
                    }
                    AddRecipeStep.DETAILS -> {
                        val productWrapper = tempSelectedProduct

                        if (productWrapper != null) {
                            if (productWrapper.essentialFood != null || productWrapper.mealApi != null) {
                                ProductDetailsScreen(
                                    alimentation = productWrapper,
                                    selectedDate = "",
                                    selectedMeal = "",
                                    registeredAlimentationViewModel = registeredAlimentationViewModel,
                                    onBack = {
                                        currentStep = AddRecipeStep.FORM
                                        tempSelectedProduct = null
                                    },
                                    onYieldResult = { amount, pieces ->
                                        activeIngredientIndex?.let { idx ->
                                            // 🔹 Zapisujemy do formularza (Local lub API)
                                            val newEntry = IngredientFormEntry(
                                                essentialFood = productWrapper.essentialFood,
                                                essentialApi = productWrapper.mealApi,
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
                                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    Text("Error: Missing product data")
                                    Button(onClick = { currentStep = AddRecipeStep.FORM }) { Text("Back") }
                                }
                            }
                        } else {
                            LaunchedEffect(Unit) { currentStep = AddRecipeStep.FORM }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddRecipeForm(
    name: String,
    desc: String,
    imageUrl: String?,
    selectedImageUri: Uri?,
    ingredients: MutableList<IngredientFormEntry>,
    isNameError: Boolean,
    nameErrorMessage: String?,
    isDescError: Boolean,
    descErrorMessage: String?,
    serverErrorMessage: String?,
    onNameChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onStartAddIngredient: () -> Unit,
    onSelectIngredient: (Int) -> Unit,
    onDeleteIngredient: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Image Picker
        item {
            ImagePicker(
                selectedImageUri = selectedImageUri,
                existingImageUrl = imageUrl,
                onImageSelected = onImageSelected
            )
        }

        // 2. Basic Info
        item {
            TextInput(
                value = name,
                onValueChange = onNameChange,
                label = "Recipe Name",
                isError = isNameError,
                errorMessage = nameErrorMessage
            )
        }
        item {
            TextInput(
                value = desc,
                onValueChange = onDescChange,
                label = "Description",
                isError = isDescError,
                errorMessage = descErrorMessage,
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )
        }

        // 3. Ingredients Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ingredients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onStartAddIngredient) {
                    Icon(Icons.Default.Add, contentDescription = "Add Ingredient", tint = Color(0xFF2E7D32))
                }
            }
        }

        // 4. Ingredients List
        if (ingredients.isEmpty()) {
            item {
                Text("No ingredients added.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        } else {
            itemsIndexed(ingredients) { index, ingredient ->
                // 🔹 Wyświetlanie nazwy niezależnie od źródła (Local vs API)
                val itemName = ingredient.essentialFood?.name
                    ?: ingredient.essentialApi?.name
                    ?: "Unknown Ingredient"

                val amountText = when {
                    ingredient.pieces != null && ingredient.pieces > 0 -> "${ingredient.pieces} pieces"
                    ingredient.amount != null -> "${ingredient.amount} g/ml"
                    else -> "-"
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).clickable { onSelectIngredient(index) }) {
                            Text(itemName, fontWeight = FontWeight.SemiBold)
                            Text(amountText, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        IconButton(onClick = { onDeleteIngredient(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                        }
                    }
                }
            }
        }

        // 5. Submit Button
        item {
            if (serverErrorMessage != null) {
                Text(serverErrorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(16.dp))
            DisplayButton(
                text = "Save Recipe",
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            )
        }
    }
}