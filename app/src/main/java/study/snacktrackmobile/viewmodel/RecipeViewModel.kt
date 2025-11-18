package study.snacktrackmobile.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.model.Recipe
import study.snacktrackmobile.data.model.dto.RecipeRequest
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.data.repository.RecipeRepository
import study.snacktrackmobile.data.storage.TokenStorage

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _recipes = MutableStateFlow<List<RecipeResponse>>(emptyList())
    val recipes: StateFlow<List<RecipeResponse>> = _recipes

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _screen = MutableStateFlow("My recipes")
    val screen: StateFlow<String> = _screen

    fun setScreen(screen: String) { _screen.value = screen }

    fun loadAllRecipes(token: String) = viewModelScope.launch {
        try {
            _recipes.value = repository.getAllRecipes(token)
        } catch (e: Exception) {
            _errorMessage.value = e.message
        }
    }

    fun loadMyRecipes(token: String) = viewModelScope.launch {
        try {
            _recipes.value = repository.getMyRecipes(token)
        } catch (e: Exception) {
            _errorMessage.value = e.message
        }
    }

    fun loadMyFavourites(token: String) = viewModelScope.launch {
        try {
            _recipes.value = repository.getMyFavourites(token)
        } catch (e: Exception) {
            _errorMessage.value = e.message
        }
    }

    fun addRecipe(token: String, request: RecipeRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // Zmieniamy logikę na oczekiwanie obiektu Result (w RecipeRepository to obsłużymy)
                val result = repository.addRecipe(token, request)

                if (result.isSuccess) {
                    loadMyRecipes(token)
                    onSuccess()
                } else {
                    // Przekazujemy wiadomość z błędem z Result/Repository do View
                    onError(result.exceptionOrNull()?.message ?: "Add recipe failed")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun updateRecipe(token: String, id: Int, request: RecipeRequest) = viewModelScope.launch {
        try {
            repository.updateRecipe(token, id, request)
            loadMyRecipes(token)
        } catch (e: Exception) {
            _errorMessage.value = e.message
        }
    }

    fun deleteRecipe(token: String, id: Int) = viewModelScope.launch {
        try {
            repository.deleteRecipe(token, id)
            _recipes.value = _recipes.value.filterNot { it.id == id }
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