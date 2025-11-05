package study.snacktrackmobile.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.model.LoginResponse
import study.snacktrackmobile.data.repository.UserRepository
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.state.UiState

class UserViewModel : ViewModel() {

    private val repository = UserRepository()

    // Flow do obserwowania stanu logowania
    private val _loginState = MutableStateFlow<UiState<LoginResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<LoginResponse>> = _loginState

    // Funkcja logowania
    fun login(email: String, password: String, context: Context) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            try {
                val response: LoginResponse = repository.login(email, password)

                response.token?.let { token ->
                    TokenStorage.saveToken(context, token)

                // ✅ Login udany → konto aktywne
                _loginState.value = UiState.Success(response)
                }
            } /*catch (e: retrofit2.HttpException) {
                // ✅ Backend blokuje logowanie dla nieaktywnych kont → używamy kodu
                if (e.code() == 401 || e.code() == 403) {
                    _loginState.value = UiState.Error("Check your email to activate your account")
                } else {
                    _loginState.value = UiState.Error("Invalid email or password")
                }
*/
            catch (e: Exception) {
                _loginState.value = UiState.Error(e.message ?: "Unexpected error occurred")
            }
        }
    }

    private val _registerState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val registerState: StateFlow<UiState<String>> = _registerState
    fun register(email: String, password: String, name: String, surname: String) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            val result = repository.register(email, password, name, surname)
            result
                .onSuccess { message ->
                    Log.d("Register", "Success: $message")
                    _registerState.value = UiState.Success(message)
                }
                .onFailure { error ->
                    Log.e("RegisterError", "Failure: ${error.message}", error)
                    _registerState.value = UiState.Error(error.message ?: "Unknown error")
                }
        }

        // Możesz dodać inne funkcje np. updateProfile, logout itd.
    }
}