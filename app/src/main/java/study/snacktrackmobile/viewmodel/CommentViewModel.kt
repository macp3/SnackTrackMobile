package study.snacktrackmobile.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import study.snacktrackmobile.data.model.dto.CommentResponse
import study.snacktrackmobile.data.repository.CommentRepository
import study.snacktrackmobile.data.storage.TokenStorage

class CommentViewModel(private val repository: CommentRepository) : ViewModel() {

    private val _comments = MutableStateFlow<List<CommentResponse>>(emptyList())
    val comments: StateFlow<List<CommentResponse>> = _comments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId

    private val _authorNames = MutableStateFlow<Map<Int, String>>(emptyMap())
    val authorNames: StateFlow<Map<Int, String>> = _authorNames

    fun setCurrentUserId(id: Int) {
        _currentUserId.value = id
    }

    fun loadComments(context: Context, mealId: Int) = viewModelScope.launch {
        val token = TokenStorage.getToken(context) ?: return@launch

        _isLoading.value = true
        val result = repository.getCommentsForMeal(token, mealId)

        result.onSuccess {
            _comments.value = it
        }.onFailure {
            _comments.value = emptyList()
        }
        _isLoading.value = false
    }

    fun addComment(context: Context, mealId: Int, content: String) = viewModelScope.launch {
        val token = TokenStorage.getToken(context) ?: return@launch
        val result = repository.addComment(token, mealId, content)

        result.onSuccess {
            Toast.makeText(context, "Comment added!", Toast.LENGTH_SHORT).show()
            loadComments(context, mealId)
        }.onFailure { e ->
            val errorMsg = if (e is HttpException) {
                try {
                    e.response()?.errorBody()?.string() ?: "HTTP ${e.code()}"
                } catch (ex: Exception) {
                    e.message ?: "Unknown error"
                }
            } else {
                e.message ?: "Connection error"
            }

            Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_LONG).show()
            Log.e("CommentsDebug", "Add comment failed: $errorMsg")
        }
    }

    fun editComment(context: Context, mealId: Int, content: String) = viewModelScope.launch {
        val token = TokenStorage.getToken(context) ?: return@launch
        val result = repository.editComment(token, mealId, content)
        result.onSuccess {
            Toast.makeText(context, "Comment updated", Toast.LENGTH_SHORT).show()
            loadComments(context, mealId)
        }.onFailure {
            Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteComment(context: Context, mealId: Int) = viewModelScope.launch {
        val token = TokenStorage.getToken(context) ?: return@launch
        val result = repository.deleteComment(token, mealId)
        result.onSuccess {
            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            loadComments(context, mealId)
        }.onFailure {
            Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun reportComment(context: Context, commentId: Int, reason: String) = viewModelScope.launch {
        val token = TokenStorage.getToken(context) ?: return@launch
        val result = repository.reportComment(token, commentId, reason)
        result.onSuccess {
            Toast.makeText(context, "Report sent", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, "Report failed", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CommentViewModel(CommentRepository()) as T
            }
        }
    }

    fun toggleLike(context: Context, commentId: Int) = viewModelScope.launch {
        val token = TokenStorage.getToken(context) ?: return@launch

        val currentList = _comments.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == commentId }

        if (index != -1) {
            val oldItem = currentList[index]
            val newIsLiked = !oldItem.isLiked
            val newCount = if (newIsLiked) oldItem.likesCount + 1 else oldItem.likesCount - 1

            currentList[index] = oldItem.copy(isLiked = newIsLiked, likesCount = newCount)
            _comments.value = currentList
        }

        val result = repository.toggleLike(token, commentId)

        result.onFailure {
            Toast.makeText(context, "Failed to like", Toast.LENGTH_SHORT).show()
            val oldItem = _comments.value.find { it.id == commentId }
            if (oldItem != null) loadComments(context, oldItem.mealId)
        }
    }
}
