package study.snacktrackmobile.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.model.dto.RecipeRequest
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.data.repository.RecipeRepository

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

    fun addRecipe(token: String, request: RecipeRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.addRecipe(token, request)
                if (result.isSuccess) {
                    loadMyRecipes(token) // Przeładuj listę po dodaniu
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Add recipe failed")
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

    companion object {
        fun provideFactory(repo: RecipeRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return RecipeViewModel(repo) as T
                }
            }
    }
}