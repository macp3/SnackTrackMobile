package study.snacktrackmobile.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

    fun loadProfile(token: String) {
        viewModelScope.launch {
            _loading.value = true
            _unauthorized.value = false
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

    private fun buildImageUrl(path: String?): String? {
        if (path.isNullOrEmpty()) {
            return ApiConfig.BASE_URL + "/images/profiles/default_profile_picture.png"
        }
        return if (path.startsWith("http")) path else ApiConfig.BASE_URL + path
    }

    fun uploadImage(token: String, uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            try {
                val imagePart = createMultipart(uri, contentResolver)
                val response = api.uploadImage("Bearer $token", imagePart)
                if (response.isSuccessful) {
                    val relativePath = response.body()?.string()
                    val fullUrl = buildImageUrl(relativePath)
                    _user.value = _user.value?.copy(imageUrl = fullUrl)
                } else {
                    _error.value = "Image upload failed"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun createMultipart(uri: Uri, contentResolver: ContentResolver): MultipartBody.Part {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Could not open input stream")
        val tempFile = File.createTempFile("upload_", ".jpg")
        val outputStream = FileOutputStream(tempFile)
        inputStream.use { input -> outputStream.use { output -> input.copyTo(output) } }
        val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image", tempFile.name, requestBody)
    }

    suspend fun changePassword(token: String, newPassword: String) =
        api.changePassword("Bearer $token", newPassword)

    fun setError(msg: String) {
        _error.value = msg
    }

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
