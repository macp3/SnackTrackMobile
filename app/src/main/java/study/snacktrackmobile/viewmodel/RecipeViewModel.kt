package study.snacktrackmobile.viewmodel

import android.content.Context
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
import study.snacktrackmobile.data.storage.TokenStorage

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _recipes = MutableStateFlow<List<RecipeResponse>>(emptyList())
    val recipes: StateFlow<List<RecipeResponse>> = _recipes

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage


    fun loadRecipes(context: Context) {
        viewModelScope.launch {
            val token = TokenStorage.getToken(context) ?: return@launch
            try {
                _recipes.value = repository.getMyRecipes(token)
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _recipes.value = emptyList()
            }
        }
    }


    fun addRecipe(context: Context, dto: RecipeRequest) {
        viewModelScope.launch {
            val token = TokenStorage.getToken(context) ?: return@launch
            repository.addRecipe(token, dto)
            loadRecipes(context)
        }
    }


    fun updateRecipe(context: Context, id: Int, dto: RecipeRequest) {
        viewModelScope.launch {
            val token = TokenStorage.getToken(context) ?: return@launch
            try {
                repository.updateRecipe(token, id, dto)
                loadRecipes(context)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }


    fun deleteRecipe(context: Context, id: Int) {
        viewModelScope.launch {
            val token = TokenStorage.getToken(context) ?: return@launch
            try {
                repository.deleteRecipe(token, id)
                _recipes.value = _recipes.value.filterNot { it.id == id }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }


    companion object {
        fun provideFactory(repository: RecipeRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return RecipeViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}
