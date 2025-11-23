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

    // ---------------------------
    // LOAD PROFILE
    // ---------------------------
    fun loadProfile(token: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = api.getProfile("Bearer $token")
                if (response.isSuccessful) {
                    val userData = response.body()
                    userData?.imageUrl = buildImageUrl(userData?.imageUrl)
                    _user.value = userData
                } else {
                    _error.value = "Failed to load profile"
                }
            } catch (e: Exception) {
                _error.value = e.message
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
    // UPLOAD PROFILE IMAGE
    // ---------------------------
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

                // 🔥 2. Zaktualizuj lokalny Flow
                _bodyParameters.value = updated

                // 🔥 3. Zaktualizuj SummaryBarState (główny UI)
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
            try { _bodyParameters.value = api.getBodyParameters("Bearer $token") }
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
