package study.snacktrackmobile.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.model.dto.RecipeRequest
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.data.repository.RecipeRepository
import study.snacktrackmobile.utils.FileUtils

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _recipes = MutableStateFlow<List<RecipeResponse>>(emptyList())
    val recipes: StateFlow<List<RecipeResponse>> = _recipes

    // Zbiór ID ulubionych przepisów - to steruje kolorem serduszek
    private val _favouriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val favouriteIds: StateFlow<Set<Int>> = _favouriteIds

    // ID zalogowanego usera (do przycisku usuwania)
    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _screen = MutableStateFlow("My recipes")
    val screen: StateFlow<String> = _screen

    fun setScreen(screen: String) { _screen.value = screen }

    fun setCurrentUserId(id: Int) { _currentUserId.value = id }

    // 🔹 Pomocnicza funkcja: Pobiera ulubione w tle, by zaktualizować serduszka
    private fun refreshFavouriteIds(token: String) = viewModelScope.launch {
        try {
            val favs = repository.getMyFavourites(token)
            _favouriteIds.value = favs.map { it.id }.toSet()
        } catch (e: Exception) {
            // Błąd pobierania ulubionych nie powinien blokować UI, logujemy cicho
            e.printStackTrace()
        }
    }

    fun loadAllRecipes(token: String) = viewModelScope.launch {
        try {
            _recipes.value = repository.getAllRecipes(token)
            // 🔹 WAŻNE: Po pobraniu listy, pobierz też ulubione, żeby oznaczyć serduszka
            refreshFavouriteIds(token)
        } catch (e: Exception) {
            _errorMessage.value = e.message
        }
    }

    fun loadMyRecipes(token: String) = viewModelScope.launch {
        try {
            _recipes.value = repository.getMyRecipes(token)
            // 🔹 WAŻNE: Tutaj też odświeżamy stan serduszek
            refreshFavouriteIds(token)
        } catch (e: Exception) {
            _errorMessage.value = e.message
        }
    }

    fun loadMyFavourites(token: String) = viewModelScope.launch {
        try {
            val favs = repository.getMyFavourites(token)
            _recipes.value = favs
            // Aktualizujemy listę ID
            _favouriteIds.value = favs.map { it.id }.toSet()
        } catch (e: Exception) {
            _errorMessage.value = e.message
        }
    }

    // 🔹 Logika przełączania serduszka
    fun toggleFavourite(token: String, recipe: RecipeResponse) = viewModelScope.launch {
        val isCurrentlyFav = _favouriteIds.value.contains(recipe.id)

        val success = if (isCurrentlyFav) {
            repository.removeFavourite(token, recipe.id)
        } else {
            repository.addFavourite(token, recipe.id)
        }

        if (success) {
            // Aktualizujemy lokalny zbiór ID (optymistycznie lub po sukcesie)
            val currentSet = _favouriteIds.value.toMutableSet()
            if (isCurrentlyFav) {
                currentSet.remove(recipe.id)
                // Jeśli jesteśmy na ekranie "Favourites", usuwamy też przepis z widocznej listy
                if (_screen.value == "Favourites") {
                    _recipes.value = _recipes.value.filter { it.id != recipe.id }
                }
            } else {
                currentSet.add(recipe.id)
            }
            _favouriteIds.value = currentSet
        } else {
            _errorMessage.value = "Failed to update favourite"
        }
    }

    fun addRecipe(token: String, request: RecipeRequest, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.addRecipe(token, request)
                result.onSuccess { newId ->
                    loadMyRecipes(token) // Odśwież listę
                    onSuccess(newId)     // 👈 Przekaż ID do widoku
                }.onFailure { error ->
                    onError(error.message ?: "Add recipe failed")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteRecipe(token: String, id: Int) = viewModelScope.launch {
        try {
            val success = repository.deleteRecipe(token, id)
            if (success) {
                _recipes.value = _recipes.value.filterNot { it.id == id }
            } else {
                _errorMessage.value = "Failed to delete recipe"
            }
        } catch (e: Exception) {
            _errorMessage.value = e.message
        }
    }

    fun updateRecipe(token: String, id: Int, request: RecipeRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // Zakładam, że w repozytorium masz metodę updateRecipe zwracającą Boolean
                // Jeśli repo zwraca Response, logika może wymagać drobnej zmiany (jak w addRecipe)
                val success = repository.updateRecipe(token, id, request)
                if (success) {
                    loadMyRecipes(token) // Odśwież listę po edycji
                    onSuccess()
                } else {
                    onError("Failed to update recipe (Server returned false)")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error during update")
            }
        }
    }

    fun uploadRecipeImage(
        context: Context,
        token: String,
        recipeId: Int,
        imageUri: Uri,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            // 1. Konwersja Uri -> File (robimy to w Dispatchers.IO dla bezpieczeństwa)
            val file = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                FileUtils.getFileFromUri(context, imageUri)
            }

            if (file != null) {
                // 2. Upload
                val success = repository.uploadImage(token, recipeId, file)
                if (success) {
                    // Odśwież dane, żeby pobrać nowy URL z backendu
                    loadMyRecipes(token)
                    onSuccess()
                } else {
                    onError("Failed to upload image")
                }
            } else {
                onError("Could not process image file")
            }
        }
    }

    // Dodaj w RecipeViewModel

    // W RecipeViewModel.kt

    fun openRecipeDetails(
        token: String,
        recipeId: Int,
        onSuccess: (RecipeResponse) -> Unit,
        onError: (String) -> Unit // <--- NOWY PARAMETR
    ) {
        viewModelScope.launch {
            val result = repository.getRecipeDetails(token, recipeId)

            result.onSuccess { fullRecipe ->
                onSuccess(fullRecipe)
            }.onFailure { e ->
                _errorMessage.value = "Failed to load details: ${e.message}"
                onError(e.message ?: "Unknown error") // <--- Wywołujemy błąd
            }
        }
    }

    companion object {
        fun provideFactory(repo: RecipeRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return RecipeViewModel(repo) as T
                }
            }
    }

    private var searchJob: Job? = null

    fun searchRecipes(token: String, query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            loadAllRecipes(token) // Jeśli puste, ładuj "Discover" (wszystkie)
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce 500ms
            try {
                val results = repository.searchRecipes(token, query)
                _recipes.value = results
                refreshFavouriteIds(token) // Żeby serduszka działały
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun reportRecipe(token: String, recipeId: Int, reason: String) = viewModelScope.launch {
        val result = repository.reportRecipe(token, recipeId, reason)
        result.onSuccess {
            // Możesz tu dodać np. _toastMessage.value = "Reported" jeśli masz taki mechanizm
            // Lub po prostu nic nie robić, UI obsłuży sukces zamknięciem dialogu
        }.onFailure {
            _errorMessage.value = "Failed to report recipe: ${it.message}"
        }
    }
}