package study.snacktrackmobile.presentation.ui.views

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import study.snacktrackmobile.presentation.ui.components.DisplayButton
import study.snacktrackmobile.presentation.ui.components.PasswordInput
import study.snacktrackmobile.presentation.ui.components.SnackTrackTopBar
import study.snacktrackmobile.presentation.ui.components.montserratFont
import study.snacktrackmobile.presentation.ui.state.UiState
import study.snacktrackmobile.viewmodel.UserViewModel
import study.snacktrackmobile.data.model.LoginResponse
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import study.snacktrackmobile.R
import study.snacktrackmobile.presentation.ui.components.TextInput

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

    fun validateLoginFrontend(): Boolean {
        emailError = email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
        passwordError = password.isBlank() || password.length < 6

        validationMessage = when {
            emailError && email.isBlank() -> "Email cannot be empty"
            emailError -> "Invalid email format"
            passwordError && password.isBlank() -> "Password cannot be empty"
            passwordError -> "Password must have at least 6 characters"
            else -> null
        }

        return validationMessage == null
    }

    val backendMessage = when (loginState) {
        is UiState.Error -> (loginState as UiState.Error).message
        is UiState.Success -> {
            val response = (loginState as UiState.Success<LoginResponse>).data
            response.message
        }
        else -> null
    }

    val displayedErrorMessage = backendMessage ?: validationMessage

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

    LoginFormContent(
        email = email,
        onEmailChange = {
            email = it
            emailError = false
            validationMessage = null
        },
        password = password,
        onPasswordChange = {
            password = it
            passwordError = false
            validationMessage = null
        },
        emailError = emailError,
        passwordError = passwordError,
        errorMessage = displayedErrorMessage,
        onLoginClick = {
            if (validateLoginFrontend()) {
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
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.ime.asPaddingValues()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SnackTrackTopBar()

        Spacer(modifier = Modifier.height(20.dp))

        Image(
            painter = painterResource(id = R.drawable.logo_vector),
            contentDescription = "App logo",
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .aspectRatio(1f)
                .padding(bottom = 20.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Welcome back!",
            fontFamily = montserratFont,
            fontSize = 36.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(25.dp))

        TextInput(
            value = email,
            label = "Email",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailError,
            modifier = Modifier.fillMaxWidth(0.85f),
            onValueChange = onEmailChange
        )

        Spacer(modifier = Modifier.height(15.dp))

        PasswordInput(
            value = password,
            label = "Password",
            onValueChange = onPasswordChange,
            isError = passwordError,
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(15.dp))

        if (!errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                fontFamily = montserratFont
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        DisplayButton(
            "Login",
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(0.5f)
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}
