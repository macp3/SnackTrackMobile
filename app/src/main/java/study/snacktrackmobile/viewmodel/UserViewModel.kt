package study.snacktrackmobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.model.LoginResponse
import study.snacktrackmobile.data.repository.UserRepository
import study.snacktrackmobile.presentation.ui.state.UiState

class UserViewModel : ViewModel() {

    private val repository = UserRepository()

    // Flow do obserwowania stanu logowania
    private val _loginState = MutableStateFlow<UiState<LoginResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<LoginResponse>> = _loginState

    // Funkcja logowania
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            try {
                val response: LoginResponse = repository.login(email, password)
                Log.d("Login", "Token: ${response.token}")
                _loginState.value = UiState.Success(response)
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.localizedMessage ?: "Nieznany błąd")
            }
        }
    }

    // Możesz dodać inne funkcje np. updateProfile, logout itd.
}