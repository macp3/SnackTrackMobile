package study.snacktrackmobile.presentation.ui.views

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import study.snacktrackmobile.presentation.ui.components.DisplayButton
import study.snacktrackmobile.presentation.ui.components.PasswordInput
import study.snacktrackmobile.presentation.ui.components.SnackTrackTopBar
import study.snacktrackmobile.presentation.ui.components.montserratFont
import study.snacktrackmobile.presentation.ui.state.UiState
import study.snacktrackmobile.viewmodel.UserViewModel
import study.snacktrackmobile.data.model.LoginResponse
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun LoginView(
    navController: NavController,
    viewModel: UserViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    val loginState by viewModel.loginState.collectAsState()
    val context = LocalContext.current

    fun validateLogin(): Boolean {
        val emailTrimmed = email.trim()
        val passwordTrimmed = password.trim()

        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()
        val isPasswordValid = passwordTrimmed.length >= 6

        emailError = !isEmailValid
        passwordError = !isPasswordValid

        validationMessage = when {
            !isEmailValid -> "Invalid email format"
            !isPasswordValid -> "Password must have at least 6 characters"
            else -> null
        }

        return isEmailValid && isPasswordValid
    }

    // Obsługa routingu po zalogowaniu na podstawie showSurvey
    LaunchedEffect(loginState) {
        if (loginState is UiState.Success) {
            val response = (loginState as UiState.Success<LoginResponse>).data
            if (response.showSurvey) {
                navController.navigate("InitialSurveyView") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                navController.navigate("MainView") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
    }

    // Pobranie komunikatu z backendu
    val backendMessage = when (loginState) {
        is UiState.Error -> (loginState as UiState.Error).message
        else -> null
    }

    LoginFormContent(
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        emailError = emailError,
        passwordError = passwordError,
        errorMessage = backendMessage ?: validationMessage,
        onLoginClick = {
            if (validateLogin()) {
                viewModel.login(email.trim(), password.trim(), context)
            }
        }
    )
}

@Composable
fun LoginFormContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    emailError: Boolean,
    passwordError: Boolean,
    errorMessage: String?,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SnackTrackTopBar()

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text("Welcome back!", fontFamily = montserratFont, fontSize = 36.sp)
            Spacer(modifier = Modifier.height(35.dp))

            // EMAIL INPUT
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = {
                    Text(
                        "Email",
                        fontFamily = montserratFont,
                        fontSize = 18.sp,
                        color = if (emailError) Color.Red else Color.Black
                    )
                },
                placeholder = {
                    Text(
                        "name@example.com",
                        fontFamily = montserratFont,
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .width(300.dp)
                    .background(Color.Transparent, shape = RoundedCornerShape(12.dp)),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                textStyle = TextStyle(fontSize = 18.sp, fontFamily = montserratFont, color = Color.Black),
                shape = RoundedCornerShape(12.dp),
                isError = emailError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (emailError) Color.Red else Color.Black,
                    unfocusedBorderColor = if (emailError) Color.Red else Color.Gray,
                    errorBorderColor = Color.Red,
                    focusedLabelColor = if (emailError) Color.Red else Color.Black,
                    unfocusedLabelColor = if (emailError) Color.Red else Color.Black
                )
            )

            Spacer(modifier = Modifier.height(15.dp))

            // PASSWORD INPUT
            PasswordInput(
                value = password,
                label = "Password",
                onValueChange = onPasswordChange,
                isError = passwordError
            )

            Spacer(modifier = Modifier.height(15.dp))

            // KOMUNIKAT BŁĘDU (walidacja frontend / backend)
            if (!errorMessage.isNullOrEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontFamily = montserratFont
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            DisplayButton("Login", onClick = onLoginClick)
        }
    }
}
