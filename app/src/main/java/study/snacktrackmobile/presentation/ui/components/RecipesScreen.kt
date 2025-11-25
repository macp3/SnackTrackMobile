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
import androidx.compose.material.icons.filled.Search
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
import study.snacktrackmobile.data.model.dto.ApiFoodResponseDetailed
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.IngredientRequest
import study.snacktrackmobile.data.model.dto.RecipeRequest
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.repository.UserRepository
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.CommentViewModel
import study.snacktrackmobile.viewmodel.FoodViewModel
import study.snacktrackmobile.viewmodel.RecipeViewModel
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

enum class AddRecipeStep {
    FORM,
    SEARCH,
    DETAILS
}

// Constant for the internal add mode
const val MODE_ADD_RECIPE = "Add recipe_INTERNAL"

@Composable
fun RecipesScreen(
    viewModel: RecipeViewModel,
    foodViewModel: FoodViewModel,
    navController: NavController,
    registeredAlimentationViewModel: RegisteredAlimentationViewModel,
    selectedDate: String? = null,
    recipeToOpen: RecipeResponse? = null,
    onRecipeOpened: () -> Unit = {},
    commentViewModel: CommentViewModel
) {
    val context = LocalContext.current
    val userRepository: UserRepository by lazy { UserRepository() }

    // Observing screen state
    val selectedMode by viewModel.screen.collectAsState()

    val recipes by viewModel.recipes.collectAsState()
    val favouriteIds by viewModel.favouriteIds.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedRecipeDetails by remember { mutableStateOf<RecipeResponse?>(null) }
    var isLoadingDetails by remember { mutableStateOf(false) }

    var token by remember { mutableStateOf("") }

    // --- SEARCH (Discover) ---
    var searchQuery by remember { mutableStateOf("") }

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
    var tempSelectedProduct by remember { mutableStateOf<RegisteredAlimentationResponse?>(null) }

    LaunchedEffect(Unit) {
        TokenStorage.getToken(context)?.let { t ->
            token = t
            userRepository.getUserId(t).onSuccess { id ->
                viewModel.setCurrentUserId(id)       // RecipeViewModel
                commentViewModel.setCurrentUserId(id) // CommentViewModel
            }
            // Default load "My recipes" on start if that is the state
            if (selectedMode == "My recipes") viewModel.loadMyRecipes(t)
        }
    }

    LaunchedEffect(recipeToOpen) {
        if (recipeToOpen != null && token.isNotEmpty()) {
            isLoadingDetails = true
            viewModel.openRecipeDetails(token, recipeToOpen.id,
                onSuccess = { fullRecipe ->
                    selectedRecipeDetails = fullRecipe
                    isLoadingDetails = false
                    onRecipeOpened()
                },
                onError = {
                    isLoadingDetails = false
                    Toast.makeText(context, "Failed to open recipe", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    fun uploadImageIfSelected(recipeId: Int) {
        if (selectedImageUri != null) {
            viewModel.uploadRecipeImage(
                context = context,
                token = token,
                recipeId = recipeId,
                imageUri = selectedImageUri!!,
                onSuccess = { Toast.makeText(context, "Image saved!", Toast.LENGTH_SHORT).show() },
                onError = { error -> Toast.makeText(context, "Image failed: $error", Toast.LENGTH_LONG).show() }
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

    // Back Handler logic
    BackHandler(enabled = (selectedRecipeDetails != null || selectedMode == MODE_ADD_RECIPE)) {
        if (selectedRecipeDetails != null) {
            selectedRecipeDetails = null
        } else if (selectedMode == MODE_ADD_RECIPE) {
            if (currentStep != AddRecipeStep.FORM) {
                currentStep = AddRecipeStep.FORM
            } else {
                // Exit add mode -> go back to "My recipes"
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

        if (name.isBlank()) { isNameError = true; nameErrorMessage = "Required"; isValid = false }
        if (ingredientsForms.isEmpty()) { serverErrorMessage = "Add at least one ingredient"; isValid = false }
        return isValid
    }

    fun submitRecipe() {
        if (!validateForm()) return

        val ingredientRequests = ingredientsForms.map { form ->
            IngredientRequest(
                essentialFoodId = form.essentialFood?.id,
                essentialApiId = form.essentialApi?.id,
                amount = form.amount,
                pieces = form.pieces,
                defaultUnit = form.essentialFood?.servingSizeUnit ?: form.essentialApi?.servingSizeUnit
            )
        }

        val request = RecipeRequest(name = name, description = desc, ingredients = ingredientRequests)

        if (editingRecipeId == null) {
            viewModel.addRecipe(
                token = token,
                request = request,
                onSuccess = { newId ->
                    uploadImageIfSelected(newId)
                    clearForm()
                    viewModel.setScreen("My recipes") // Return to list
                },
                onError = { serverErrorMessage = it }
            )
        } else {
            viewModel.updateRecipe(
                token = token,
                id = editingRecipeId!!,
                request = request,
                onSuccess = {
                    uploadImageIfSelected(editingRecipeId!!)
                    clearForm()
                    viewModel.setScreen("My recipes") // Return to list
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
                essentialApi = ing.essentialApi,
                amount = ing.amount,
                pieces = ing.pieces
            )
            ingredientsForms.add(entry)
        }

        viewModel.setScreen(MODE_ADD_RECIPE)
        currentStep = AddRecipeStep.FORM
    }

    fun onRecipeClick(simpleRecipe: RecipeResponse) {
        isLoadingDetails = true
        viewModel.openRecipeDetails(token, simpleRecipe.id,
            onSuccess = { fullRecipe ->
                selectedRecipeDetails = fullRecipe
                isLoadingDetails = false
            },
            onError = {
                isLoadingDetails = false
                Toast.makeText(context, "Error loading details", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- UI RENDER ---

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. RECIPE DETAILS (Full Screen)
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
                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                },
                onToggleFavourite = { viewModel.toggleFavourite(token, r) },
                commentViewModel = commentViewModel,
                onReportRecipe = { reason ->
                    viewModel.reportRecipe(token, r.id, reason)
                    Toast.makeText(context, "Report sent", Toast.LENGTH_SHORT).show()
                }
            )
        }
        // 2. LIST / FORM
        else {
            Column(modifier = Modifier.fillMaxSize()) {

                // --- Mode Bar (Hidden in Add Mode) ---
                if (selectedMode != MODE_ADD_RECIPE) {
                    Column {
                        DropdownField(
                            label = "View",
                            selected = selectedMode,
                            options = listOf("My recipes", "Favourites", "Discover"),
                            onSelected = { mode ->
                                viewModel.setScreen(mode)
                                if (token.isNotEmpty()) {
                                    when (mode) {
                                        "My recipes" -> viewModel.loadMyRecipes(token)
                                        "Favourites" -> viewModel.loadMyFavourites(token)
                                        "Discover" -> {
                                            searchQuery = "" // Reset search
                                            viewModel.loadAllRecipes(token)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )

                        // --- Search Bar for DISCOVER ---
                        if (selectedMode == "Discover") {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    viewModel.searchRecipes(token, it)
                                },
                                label = { Text("Search recipes...", fontFamily = montserratFont) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2E7D32),
                                    focusedLabelColor = Color(0xFF2E7D32),
                                    cursorColor = Color(0xFF2E7D32)
                                )
                            )
                        }
                    }
                } else if (currentStep == AddRecipeStep.FORM) {
                    // Form Header
                    Text(
                        text = if(editingRecipeId != null) "Edit Recipe" else "New Recipe",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        fontFamily = montserratFont
                    )
                }

                // --- MAIN CONTENT ---
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedMode) {
                        "My recipes", "Favourites", "Discover" -> {
                            if (recipes.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if(selectedMode == "Discover" && searchQuery.isNotEmpty()) "No recipes found" else "No recipes yet",
                                        color = Color.Gray,
                                        fontFamily = montserratFont
                                    )
                                }
                            } else {
                                RecipeListDisplay(
                                    items = recipes,
                                    favouriteIds = favouriteIds,
                                    currentUserId = currentUserId,
                                    onClick = { onRecipeClick(it) },
                                    onToggleFavourite = { viewModel.toggleFavourite(token, it) },
                                    onDelete = { viewModel.deleteRecipe(token, it) }
                                )
                            }
                        }

                        MODE_ADD_RECIPE -> {
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
                                            val formEntry = ingredientsForms[idx]
                                            tempSelectedProduct = RegisteredAlimentationResponse(
                                                id = 0, userId = 0,
                                                essentialFood = formEntry.essentialFood,
                                                mealApi = formEntry.essentialApi,
                                                meal = null, timestamp = "", amount = 0f, pieces = 0f, mealName = ""
                                            )
                                            currentStep = AddRecipeStep.DETAILS
                                        },
                                        onDeleteIngredient = { idx -> ingredientsForms.removeAt(idx) },
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

            // --- FAB (Only in "My recipes") ---
            if (selectedMode == "My recipes") {
                FloatingActionButton(
                    onClick = {
                        clearForm()
                        viewModel.setScreen(MODE_ADD_RECIPE)
                        currentStep = AddRecipeStep.FORM
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Recipe")
                }
            }
        }

        // --- GLOBAL LOADER ---
        if (isLoadingDetails) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2E7D32))
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
    val maxNameLength = 100
    val maxDescLength = 1024

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            ImagePicker(selectedImageUri, imageUrl, onImageSelected)
        }
        item {
            TextInput(value = name, onValueChange = { if (it.length <= maxNameLength) onNameChange(it) }, label = "Recipe Name", isError = isNameError, errorMessage = nameErrorMessage, singleLine = true)
        }
        item {
            Column {
                OutlinedTextField(
                    value = desc,
                    onValueChange = { if (it.length <= maxDescLength) onDescChange(it) },
                    label = { Text("Describe how to make...", fontFamily = montserratFont, color = if (isDescError) Color.Red else Color.Black) },
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    isError = isDescError,
                    singleLine = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E7D32),
                        unfocusedBorderColor = Color.Black,
                        cursorColor = Color(0xFF2E7D32),
                        focusedLabelColor = Color(0xFF2E7D32)
                    )
                )
                if (isDescError && !descErrorMessage.isNullOrBlank()) {
                    Text(text = descErrorMessage, color = Color.Red, fontSize = 12.sp, fontFamily = montserratFont, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                }
                Text("${desc.length} / $maxDescLength", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.End, style = MaterialTheme.typography.bodySmall, color = if (desc.length >= maxDescLength) Color.Red else Color.Gray, fontFamily = montserratFont)
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Ingredients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = montserratFont)
                IconButton(onClick = onStartAddIngredient) { Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF2E7D32)) }
            }
        }
        if (ingredients.isEmpty()) {
            item { Text("No ingredients added.", color = Color.Gray, fontFamily = montserratFont) }
        } else {
            itemsIndexed(ingredients) { index, ingredient ->
                val itemName = ingredient.essentialFood?.name ?: ingredient.essentialApi?.name ?: "Unknown"
                val amountText = if (ingredient.pieces != null && ingredient.pieces > 0) "${ingredient.pieces} pieces" else "${ingredient.amount} g/ml"

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f).clickable { onSelectIngredient(index) }) {
                            Text(itemName, fontWeight = FontWeight.SemiBold, fontFamily = montserratFont)
                            Text(amountText, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontFamily = montserratFont)
                        }
                        IconButton(onClick = { onDeleteIngredient(index) }) { Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red) }
                    }
                }
            }
        }
        item {
            if (serverErrorMessage != null) Text(serverErrorMessage, color = Color.Red, fontFamily = montserratFont)
            Spacer(modifier = Modifier.height(16.dp))
            DisplayButton(text = "Save Recipe", onClick = onSubmit, modifier = Modifier.fillMaxWidth().height(50.dp))
        }
    }
}