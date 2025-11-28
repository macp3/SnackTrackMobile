package study.snacktrackmobile.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import study.snacktrackmobile.data.model.dto.RecipeRequest
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.data.repository.RecipeRepository
import study.snacktrackmobile.utils.FileUtils
import java.io.File
import java.io.FileOutputStream

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _recipes = MutableStateFlow<List<RecipeResponse>>(emptyList())
    val recipes: StateFlow<List<RecipeResponse>> = _recipes

    private val _favouriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val favouriteIds: StateFlow<Set<Int>> = _favouriteIds

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _screen = MutableStateFlow("My recipes")
    val screen: StateFlow<String> = _screen

    fun setScreen(screen: String) { _screen.value = screen }

    fun setCurrentUserId(id: Int) { _currentUserId.value = id }

    private fun refreshFavouriteIds(token: String) = viewModelScope.launch {
        try {
            val favs = repository.getMyFavourites(token)
            _favouriteIds.value = favs.map { it.id }.toSet()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadAllRecipes(token: String) = viewModelScope.launch {
        try {
            _recipes.value = repository.getAllRecipes(token)
            refreshFavouriteIds(token)
        } catch (e: Exception) {
            _errorMessage.value = e.message
        }
    }

    fun loadMyRecipes(token: String) = viewModelScope.launch {
        try {
            _recipes.value = repository.getMyRecipes(token)
            refreshFavouriteIds(token)
        } catch (e: Exception) {
            _errorMessage.value = e.message
        }
    }

    fun loadMyFavourites(token: String) = viewModelScope.launch {
        try {
            val favs = repository.getMyFavourites(token)
            _recipes.value = favs
            _favouriteIds.value = favs.map { it.id }.toSet()
        } catch (e: Exception) {
            _errorMessage.value = e.message
        }
    }

    fun toggleFavourite(token: String, recipe: RecipeResponse) = viewModelScope.launch {
        val isCurrentlyFav = _favouriteIds.value.contains(recipe.id)

        val success = if (isCurrentlyFav) {
            repository.removeFavourite(token, recipe.id)
        } else {
            repository.addFavourite(token, recipe.id)
        }

        if (success) {
            val currentSet = _favouriteIds.value.toMutableSet()
            if (isCurrentlyFav) {
                currentSet.remove(recipe.id)
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
                    loadMyRecipes(token)
                    onSuccess(newId)
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
                val success = repository.updateRecipe(token, id, request)
                if (success) {
                    loadMyRecipes(token)
                    onSuccess()
                } else {
                    onError("Failed to update recipe")
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
            val file = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                FileUtils.getFileFromUri(context, imageUri)
            }
            try {
                val compressedFile = withContext(Dispatchers.IO) {
                    createCompressedFile(context, imageUri)
                }

                if (compressedFile != null) {
                    val success = repository.uploadImage(token, recipeId, compressedFile)
                    if (success) {
                        loadMyRecipes(token)
                        onSuccess()
                    } else {
                        onError("Failed to upload image")
                    }
                } else {
                    onError("Could not process image file")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error processing image: ${e.message}")
            }
        }
    }

    private fun createCompressedFile(context: Context, uri: Uri): File? {
        val contentResolver = context.contentResolver

        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()

        val maxDimension = 1920
        options.inSampleSize = calculateInSampleSize(options, maxDimension, maxDimension)
        options.inJustDecodeBounds = false

        val inputStream2 = contentResolver.openInputStream(uri)
        var bitmap = BitmapFactory.decodeStream(inputStream2, null, options)
        inputStream2?.close()

        if (bitmap == null) return null

        bitmap = rotateBitmapIfRequired(contentResolver, uri, bitmap)

        val tempFile = File.createTempFile("recipe_compressed_", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.flush()
        outputStream.close()

        bitmap.recycle()

        return tempFile
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun rotateBitmapIfRequired(contentResolver: android.content.ContentResolver, uri: Uri, bitmap: Bitmap): Bitmap {
        var rotatedBitmap = bitmap
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            inputStream.close()

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }

            rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rotatedBitmap
    }

    fun openRecipeDetails(
        token: String,
        recipeId: Int,
        onSuccess: (RecipeResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.getRecipeDetails(token, recipeId)

            result.onSuccess { fullRecipe ->
                onSuccess(fullRecipe)
            }.onFailure { e ->
                _errorMessage.value = "Failed to load details: ${e.message}"
                onError(e.message ?: "Unknown error")
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
            loadAllRecipes(token)
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            try {
                val results = repository.searchRecipes(token, query)
                _recipes.value = results
                refreshFavouriteIds(token)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun reportRecipe(token: String, recipeId: Int, reason: String) = viewModelScope.launch {
        val result = repository.reportRecipe(token, recipeId, reason)
        result.onSuccess {
        }.onFailure {
            _errorMessage.value = "Failed to report recipe: ${it.message}"
        }
    }
}