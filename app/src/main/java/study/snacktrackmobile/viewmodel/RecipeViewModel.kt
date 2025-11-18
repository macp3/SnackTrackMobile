package study.snacktrackmobile.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
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

    private val _favouritesIds = MutableStateFlow<Set<Int>>(emptySet())
    // EXPOSED PUBLIC FLOW - tego brakowało
    val favouritesIds: StateFlow<Set<Int>> = _favouritesIds

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _screen = MutableStateFlow("My recipes")
    val screen: StateFlow<String> = _screen

    fun loadAllRecipes(token: String) {
        viewModelScope.launch {
            refreshFavouritesInternal(token)
            val list = repository.getAllRecipes(token)
            _recipes.value = list // Lista jest oznaczana w UI na podstawie zbioru favouritesIds
        }
    }

    fun loadMyRecipes(token: String) {
        viewModelScope.launch {
            refreshFavouritesInternal(token)
            val list = repository.getMyRecipes(token)
            _recipes.value = list
        }
    }

    fun loadFavouriteRecipes(token: String) {
        viewModelScope.launch {
            val list = repository.getMyFavourites(token)
            _favouritesIds.value = list.map { it.id }.toSet()
            _recipes.value = list
        }
    }

    // Helper do odświeżania samych ID ulubionych
    private suspend fun refreshFavouritesInternal(token: String) {
        try {
            val favs = repository.getMyFavourites(token)
            _favouritesIds.value = favs.map { it.id }.toSet()
        } catch (e: Exception) {
            // Obsługa błędu pobierania ulubionych
        }
    }

    fun toggleFavourite(token: String, recipeId: Int) {
        viewModelScope.launch {
            val isFav = _favouritesIds.value.contains(recipeId)

            if (!isFav) {
                repository.addFavourite(token, recipeId)
            } else {
                repository.removeFavourite(token, recipeId)
            }

            // Odśwież listę ID ulubionych
            refreshFavouritesInternal(token)

            // Jeśli jesteśmy na ekranie "Favourites", musimy przeładować całą listę, żeby usunięty element zniknął
            if (_screen.value == "Favourites") {
                loadFavouriteRecipes(token)
            }
        }
    }

    fun addRecipe(token: String, request: RecipeRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.addRecipe(token, request)
            result.onSuccess { onSuccess() }
            result.onFailure { onError(it.message ?: "Unknown error") }
        }
    }

    // Usunąłem callback z sygnatury, bo loadMyRecipes wewnątrz załatwia sprawę
    fun deleteRecipe(token: String, id: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteRecipe(token, id)
            // Po usunięciu odśwież widok
            if (_screen.value == "My recipes") {
                loadMyRecipes(token)
            } else if (_screen.value == "Favourites") {
                loadFavouriteRecipes(token)
            } else {
                loadAllRecipes(token)
            }
            onComplete()
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