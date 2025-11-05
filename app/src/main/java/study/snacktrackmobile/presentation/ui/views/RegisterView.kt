package study.snacktrackmobile.presentation.ui.views

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import study.snacktrackmobile.presentation.ui.components.DisplayButton
import study.snacktrackmobile.presentation.ui.components.PasswordInput
import study.snacktrackmobile.presentation.ui.components.SnackTrackTopBar
import study.snacktrackmobile.presentation.ui.components.TextInput
import study.snacktrackmobile.presentation.ui.components.montserratFont
import study.snacktrackmobile.presentation.ui.state.UiState
import study.snacktrackmobile.viewmodel.UserViewModel

@Composable
fun RegisterView(
    navController: NavController,
    viewModel: UserViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var surnameError by remember { mutableStateOf(false) }

    var validationMessage by remember { mutableStateOf<String?>(null) }

    val registerState by viewModel.registerState.collectAsState()
    val backendMessage = (registerState as? UiState.Error)?.message

    fun validateRegister(): Boolean {
        validationMessage = null

        val emailValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        val passwordValid = password.trim().length >= 6
        val passwordMatch = confirmPassword.trim() == password.trim()
        val confirmValid = confirmPassword.trim().isNotEmpty() && passwordMatch && passwordValid
        val nameValid = name.trim().isNotEmpty()
        val surnameValid = surname.trim().isNotEmpty()

        emailError = !emailValid
        passwordError = !passwordValid
        confirmPasswordError =
            confirmPassword.trim().isEmpty() || !passwordMatch || password.trim().length < 6
        nameError = !nameValid
        surnameError = !surnameValid

        validationMessage =
            when {
                !nameValid -> "Name cannot be empty"
                !surnameValid -> "Surname cannot be empty"
                !emailValid -> "Invalid email format"
                !passwordValid -> "Password must have at least 6 characters"
                confirmPassword.trim().isEmpty() -> "Confirm password is required"
                !passwordMatch -> "Passwords do not match"
                else -> null
            }

        return validationMessage == null
    }

    when (registerState) {
        is UiState.Success -> {
            navController.navigate("LoginView") {
                popUpTo("RegisterView") { inclusive = true }
            }
        }
        else -> Unit
    }

    RegisterFormContent(
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        confirmPassword = confirmPassword,
        onConfirmPasswordChange = { confirmPassword = it },
        name = name,
        onNameChange = { name = it },
        surname = surname,
        onSurnameChange = { surname = it },
        emailError = emailError,
        passwordError = passwordError,
        confirmPasswordError = confirmPasswordError,
        nameError = nameError,
        surnameError = surnameError,
        errorMessage = backendMessage ?: validationMessage,
        onRegisterClick = {
            if (validateRegister()) {
                viewModel.register(
                    email.trim(),
                    password.trim(),
                    name.trim(),
                    surname.trim()
                )
            }
        }
    )
}


@Composable
fun RegisterFormContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    surname: String,
    onSurnameChange: (String) -> Unit,
    emailError: Boolean,
    passwordError: Boolean,
    confirmPasswordError: Boolean,
    nameError: Boolean,
    surnameError: Boolean,
    errorMessage: String?,
    onRegisterClick: () -> Unit,
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
            Text("Create account", fontFamily = montserratFont, fontSize = 36.sp)
            Spacer(modifier = Modifier.height(25.dp))

            TextInput(
                value = name,
                label = "Name",
                isError = nameError,
                onValueChange = onNameChange
            )

            Spacer(modifier = Modifier.height(15.dp))

            TextInput(
                value = surname,
                label = "Surname",
                isError = surnameError,
                onValueChange = onSurnameChange
            )

            Spacer(modifier = Modifier.height(15.dp))

            TextInput(
                value = email,
                label = "Email",
                isError = emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                onValueChange = onEmailChange
            )

            Spacer(modifier = Modifier.height(15.dp))

            PasswordInput(
                value = password,
                label = "Password",
                isError = passwordError,
                onValueChange = onPasswordChange
            )

            Spacer(modifier = Modifier.height(15.dp))

            PasswordInput(
                value = confirmPassword,
                label = "Confirm Password",
                isError = confirmPasswordError,
                onValueChange = onConfirmPasswordChange
            )

            Spacer(modifier = Modifier.height(15.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontFamily = montserratFont,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            DisplayButton("Register", onClick = onRegisterClick)
        }
    }
}
