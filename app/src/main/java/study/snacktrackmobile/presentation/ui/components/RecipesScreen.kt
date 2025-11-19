package study.snacktrackmobile.presentation.ui.components

import DropdownField
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.IngredientRequest
import study.snacktrackmobile.data.model.dto.RecipeRequest
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.viewmodel.FoodViewModel
import study.snacktrackmobile.viewmodel.RecipeViewModel

// Enum do zarządzania wewnętrznymi stanami widoku "Add Recipe"
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
    onRecipeClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val selected by viewModel.screen.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var token by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        TokenStorage.getToken(context)?.let { t ->
            token = t
            viewModel.loadMyRecipes(t)
        } ?: run {
            Toast.makeText(context, "No auth token found", Toast.LENGTH_SHORT).show()
        }
    }





    // Stan formularza
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    val ingredientsForms = remember { mutableStateListOf<IngredientFormEntry>() }

    // STANY WALIDACJI I BŁĘDÓW SERWERA
    var isNameError by remember { mutableStateOf(false) }
    var nameErrorMessage by remember { mutableStateOf<String?>(null) }
    var isDescError by remember { mutableStateOf(false) }
    var descErrorMessage by remember { mutableStateOf<String?>(null) }
    var serverErrorMessage by remember { mutableStateOf<String?>(null) }

    // Stan wewnętrznej nawigacji
    var currentStep by remember { mutableStateOf(AddRecipeStep.FORM) }
    var activeIngredientIndex by remember { mutableStateOf<Int?>(null) }
    var tempSelectedProduct by remember { mutableStateOf<EssentialFoodResponse?>(null) }

    // Obsługa przycisku "Wstecz" sprzętowego w telefonie
    BackHandler(enabled = (selected == "Add recipe" && currentStep != AddRecipeStep.FORM)) {
        currentStep = AddRecipeStep.FORM
        tempSelectedProduct = null // Resetujemy stan
    }

    // 🔹 FUNKCJA WALIDACJI PO STRONIE KLIENTA
    fun validateForm(): Boolean {
        isNameError = false
        nameErrorMessage = null
        isDescError = false
        descErrorMessage = null
        serverErrorMessage = null

        var isValid = true

        // 1. Walidacja Name (max 50)
        if (name.isBlank()) {
            isNameError = true
            nameErrorMessage = "Recipe name cannot be empty."
            isValid = false
        } else if (name.length > 50) {
            isNameError = true
            nameErrorMessage = "Recipe name must be max 50 characters."
            isValid = false
        }

        // 2. Walidacja Description (max 100)
        if (desc.isBlank()) {
            isDescError = true
            descErrorMessage = "Description cannot be empty."
            isValid = false
        } else if (desc.length > 100) {
            isDescError = true
            descErrorMessage = "Description must be max 100 characters."
            isValid = false
        }

        // 3. Walidacja Składników (minimalna liczba)
        if (ingredientsForms.isEmpty()) {
            serverErrorMessage = "The meal has to have at least one ingredient."
            isValid = false
        }

        return isValid
    }

    // 🔹 FUNKCJA OBSŁUGUJĄCA ZAPIS
    fun submitRecipe() {
        if (!validateForm()) {
            return
        }

        // 1. Mapowanie IngredientFormEntry na IngredientRequest DTO
        val ingredientRequests = ingredientsForms.map { entry ->
            val defaultUnit: String? =
                entry.essentialFood?.servingSizeUnit ?: entry.essentialApi?.servingSizeUnit

            IngredientRequest(
                // Zakładamy, że essentialFood jest bezpieczne i ma ID,
                // bo wczesniejsza logika zapewnia, że tylko wybrane produkty tu trafiają
                essentialFoodId = entry.essentialFood?.id,
                essentialApiId = entry.essentialApi?.id,
                amount = entry.amount,
                pieces = entry.pieces,
                defaultUnit = defaultUnit
            )
        }

        // 2. Tworzenie RecipeRequest (zamiast MealRequest)
        val recipeRequest = RecipeRequest(
            name = name,
            description = desc,
            ingredients = ingredientRequests
        )

        // 3. Wywołanie przez ViewModel
        viewModel.addRecipe(
            token = token,
            request = recipeRequest,
            onSuccess = {
                Toast.makeText(context, "Recipe created successfully!", Toast.LENGTH_LONG).show()
                // Resetowanie formularza i powrót do listy przepisów
                name = ""
                desc = ""
                ingredientsForms.clear()
                viewModel.setScreen("My recipes")
                currentStep = AddRecipeStep.FORM // Zostajemy w Add recipe, ale wracamy do FORM
            },
            onError = { error ->
                // 🔹 Obsługa błędu zwróconego z Repository/Backendu
                serverErrorMessage = error
                Log.e("API_ERROR", "Recipe creation failed: $error")
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 0.dp)) { // Usuwamy padding, by dodać go w formie
        // ... DropdownField (kod bez zmian)
        if (selected != "Add recipe" || currentStep == AddRecipeStep.FORM) {
            DropdownField(
                label = "Mode",
                selected = selected,
                options = listOf("My recipes", "Favourites", "Discover", "Add recipe"),
                onSelected = { mode ->
                    viewModel.setScreen(mode)
                    token?.let { t ->
                        when (mode) {
                            "My recipes" -> viewModel.loadMyRecipes(t)
                            "Favourites" -> viewModel.loadMyFavourites(t)
                            "Discover" -> viewModel.loadAllRecipes(t)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // Dodajemy padding tylko do DropdownField
            )
            Spacer(modifier = Modifier.height(16.dp))
        }


        // Logika wyświetlania odpowiedniego ekranu
        when (selected) {
            "My recipes", "Favourites", "Discover" -> {
                RecipeListDisplay(items = recipes, onClick = onRecipeClick)
            }

            "Add recipe" -> {
                when (currentStep) {
                    // 1. GŁÓWNY FORMULARZ
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
                                name = it
                                // 🔹 Zmiana: Resetujemy tylko lokalny stan błędu dla tego pola
                                isNameError = false
                                nameErrorMessage = null
                            },
                            onDescChange = {
                                desc = it
                                // 🔹 Zmiana: Resetujemy tylko lokalny stan błędu dla tego pola
                                isDescError = false
                                descErrorMessage = null
                            },
                            // ... (pozostałe callbacki bez zmian)
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

                    // 2. WYSZUKIWANIE PRODUKTU
                    AddRecipeStep.SEARCH -> {
                        AddProductScreen(
                            selectedDate = "",
                            selectedMeal = "Select Ingredient",
                            navController = navController,
                            foodViewModel = foodViewModel,
                            isRecipeMode = true,
                            onProductClick = { registeredAlimentation ->
                                // Użytkownik wybrał produkt z listy
                                tempSelectedProduct = registeredAlimentation.essentialFood
                                currentStep = AddRecipeStep.DETAILS
                            }
                        )
                    }

                    // 3. SZCZEGÓŁY (ILOŚĆ/WAGA)
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
                                registeredAlimentationViewModel = study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel(
                                    study.snacktrackmobile.data.repository.RegisteredAlimentationRepository(study.snacktrackmobile.data.api.Request.api)
                                ),
                                onBack = {
                                    currentStep = AddRecipeStep.FORM
                                    tempSelectedProduct = null // Wróć i zapomnij o tymczasowym produkcie
                                },
                                onYieldResult = { amount, pieces ->
                                    // 🔹 Logika dodawania/edycji (bez zmian)
                                    activeIngredientIndex?.let { idx ->
                                        val newEntry = IngredientFormEntry(
                                            essentialFood = product,
                                            amount = amount,
                                            pieces = pieces
                                        )

                                        if (idx == ingredientsForms.size) {
                                            ingredientsForms.add(newEntry)
                                        } else {
                                            ingredientsForms[idx] = newEntry
                                        }
                                    }
                                    // 🔹 KLUCZOWA ZMIANA: ZAWSZE resetujemy stan po sukcesie
                                    tempSelectedProduct = null // Resetuj, aby umożliwić ponowne dodawanie
                                    activeIngredientIndex = null
                                    currentStep = AddRecipeStep.FORM
                                }
                            )
                        } else {
                            // W przypadku anulowania wyboru produktu w kroku Search/Details, po prostu wracamy.
                            // W stanie SEARCH/DETAILS, jeśli tempSelectedProduct jest null, wracamy do FORM.
                            LaunchedEffect(Unit) { currentStep = AddRecipeStep.FORM }
                        }
                    }
                }
            }
        }
    }
}