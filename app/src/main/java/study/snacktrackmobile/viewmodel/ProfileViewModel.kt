package study.snacktrackmobile.viewmodel

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import study.snacktrackmobile.data.api.UserApi
import study.snacktrackmobile.data.model.dto.BodyParametersRequest
import study.snacktrackmobile.data.model.dto.BodyParametersResponse
import study.snacktrackmobile.data.model.dto.UserResponse
import study.snacktrackmobile.data.network.ApiConfig
import study.snacktrackmobile.presentation.ui.state.SummaryBarState
import java.io.File
import java.io.FileOutputStream

class ProfileViewModel(private val api: UserApi) : ViewModel() {

    private val _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _bodyParameters = MutableStateFlow<BodyParametersResponse?>(null)
    val bodyParameters: StateFlow<BodyParametersResponse?> = _bodyParameters.asStateFlow()

    private val _unauthorized = MutableStateFlow(false)
    val unauthorized: StateFlow<Boolean> = _unauthorized.asStateFlow()

    // ---------------------------
    // LOAD PROFILE
    // ---------------------------
    fun loadProfile(token: String) {
        viewModelScope.launch {
            _loading.value = true
            _unauthorized.value = false // Resetujemy stan
            try {
                val response = api.getProfile("Bearer $token")

                when {
                    response.code() == 401 || response.code() == 403 -> {
                        _error.value = "unauthorized"
                        _user.value = null
                        _unauthorized.value = true
                    }

                    response.isSuccessful -> {
                        val userData = response.body()
                        userData?.imageUrl = buildImageUrl(userData?.imageUrl)
                        _user.value = userData
                        _error.value = null
                    }

                    else -> {
                        _error.value = "other_error"
                        _user.value = null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "network_error"
                _user.value = null
            }
            _loading.value = false
        }
    }

    // Convert backend path → full URL
    private fun buildImageUrl(path: String?): String? {
        if (path.isNullOrEmpty()) {
            return ApiConfig.BASE_URL + "/images/profiles/default_profile_picture.png"
        }
        return if (path.startsWith("http")) path else ApiConfig.BASE_URL + path
    }

    // ---------------------------
    // UPLOAD PROFILE IMAGE (Z KOMPRESJĄ)
    // ---------------------------
    fun uploadImage(token: String, uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _loading.value = true
            try {
                // Przenosimy ciężkie obliczenia (kompresję) na wątek IO
                val imagePart = withContext(Dispatchers.IO) {
                    createCompressedMultipart(uri, contentResolver)
                }

                val response = api.uploadImage("Bearer $token", imagePart)
                if (response.isSuccessful) {
                    val relativePath = response.body()?.string()
                    val fullUrl = buildImageUrl(relativePath)
                    _user.value = _user.value?.copy(imageUrl = fullUrl)
                    _error.value = null
                } else {
                    _error.value = "Image upload failed: ${response.code()}"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Upload error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Wczytuje zdjęcie, zmniejsza rozdzielczość (max 1920px),
     * naprawia orientację (EXIF) i kompresuje do JPG (80%).
     */
    private fun createCompressedMultipart(uri: Uri, contentResolver: ContentResolver): MultipartBody.Part {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Could not open input stream")

        // 1. Dekodujemy tylko wymiary, aby obliczyć skalę
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()

        // 2. Obliczamy inSampleSize (skalowanie do max 1920px)
        val maxDimension = 1920
        options.inSampleSize = calculateInSampleSize(options, maxDimension, maxDimension)
        options.inJustDecodeBounds = false

        // 3. Wczytujemy faktyczną, pomniejszoną Bitmapę
        val inputStream2 = contentResolver.openInputStream(uri)
        var bitmap = BitmapFactory.decodeStream(inputStream2, null, options)
        inputStream2?.close()

        if (bitmap == null) throw IllegalArgumentException("Could not decode bitmap")

        // 4. Obsługa obrotu (EXIF) - zdjęcia z aparatu często są obrócone
        bitmap = rotateBitmapIfRequired(contentResolver, uri, bitmap)

        // 5. Kompresja do pliku tymczasowego
        val tempFile = File.createTempFile("upload_compressed_", ".jpg")
        val outputStream = FileOutputStream(tempFile)

        // Kompresja: JPEG, Jakość 80% (drastycznie zmniejsza rozmiar przy zachowaniu jakości)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.flush()
        outputStream.close()

        // Zwalniamy pamięć bitmapy
        bitmap.recycle()

        // 6. Tworzymy MultipartBody
        val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image", tempFile.name, requestBody)
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

    private fun rotateBitmapIfRequired(contentResolver: ContentResolver, uri: Uri, bitmap: Bitmap): Bitmap {
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
                else -> return bitmap // Brak potrzeby obracania
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

    // ---------------------------
    // CHANGE PASSWORD
    // ---------------------------
    suspend fun changePassword(token: String, newPassword: String) =
        api.changePassword("Bearer $token", newPassword)

    // ---------------------------
    // ERROR MANAGEMENT
    // ---------------------------
    fun setError(msg: String) {
        _error.value = msg
    }

    // ---------------------------
    // BODY PARAMETERS
    // ---------------------------
    fun changeBodyParameters(token: String, request: BodyParametersRequest) {
        viewModelScope.launch {
            try {
                api.changeParameters("Bearer $token", request)
                val updated = api.getBodyParameters("Bearer $token")

                _bodyParameters.value = updated

                SummaryBarState.limitKcal = updated.calorieLimit
                SummaryBarState.limitProtein = updated.proteinLimit
                SummaryBarState.limitFat = updated.fatLimit
                SummaryBarState.limitCarbs = updated.carbohydratesLimit
            }
            catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun getBodyParameters(token: String) {
        viewModelScope.launch {
            try {
                val bodyParamsResponse = api.getBodyParameters("Bearer $token")
                _bodyParameters.value = bodyParamsResponse

                SummaryBarState.setLimits(
                    kcal = bodyParamsResponse.calorieLimit,
                    protein = bodyParamsResponse.proteinLimit,
                    fat = bodyParamsResponse.fatLimit,
                    carbs = bodyParamsResponse.carbohydratesLimit
                )
            }
            catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updatePremium(token: String, expiration: String) {
        viewModelScope.launch {
            try {
                val response = api.updatePremium(
                    token = "Bearer $token",
                    expiration = expiration
                )

                if (response.isSuccessful) {
                    _user.value = _user.value?.copy(premiumExpiration = expiration)
                } else {
                    _error.value = "Premium update failed: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}