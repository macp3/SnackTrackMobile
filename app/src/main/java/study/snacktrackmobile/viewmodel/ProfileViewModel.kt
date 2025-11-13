package study.snacktrackmobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import study.snacktrackmobile.data.api.UserApi
import study.snacktrackmobile.data.model.dto.BodyParametersRequest
import study.snacktrackmobile.data.model.dto.BodyParametersResponse
import study.snacktrackmobile.data.model.dto.UserResponse

class ProfileViewModel(private val api: UserApi) : ViewModel() {

    private val _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadProfile(token: String) {
        viewModelScope.launch {
            _loading.value = true
            val response = api.getProfile("Bearer $token")
            if (response.isSuccessful) {
                _user.value = response.body()
            } else {
                _error.value = "Failed to load profile"
            }
            _loading.value = false
        }
    }

    suspend fun changePassword(token: String, newPassword: String): Response<ResponseBody> {
        return api.changePassword("Bearer $token", newPassword)
    }

    fun setError(msg: String) {
        _error.value = msg
    }


    fun changeBodyParameters(token: String, request: BodyParametersRequest) {
        viewModelScope.launch {
            val response = api.changeParameters("Bearer $token", request)
        }
    }



    fun uploadImage(token: String, image: MultipartBody.Part) {
        viewModelScope.launch {
            val response = api.uploadImage("Bearer $token", image)
            if (response.isSuccessful) {
                _user.value = _user.value?.copy(imageUrl = response.body())
            } else {
                _error.value = "Image upload failed"
            }
        }
    }

    private val _bodyParameters = MutableStateFlow<BodyParametersResponse?>(null)
    val bodyParameters: StateFlow<BodyParametersResponse?> = _bodyParameters.asStateFlow()

    fun getBodyParameters(token: String) {
        viewModelScope.launch {
            try {
                val response = api.getBodyParameters("Bearer $token")
                _bodyParameters.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
