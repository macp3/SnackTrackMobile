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

    /** ✅ FRONTEND VALIDATION */
    fun validateRegisterFrontend(): Boolean {
        nameError = name.isBlank()
        surnameError = surname.isBlank()
        emailError = email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
        passwordError = password.isBlank() || password.length < 6
        confirmPasswordError = confirmPassword.isBlank() || confirmPassword != password

        validationMessage = when {
            nameError -> "Name cannot be empty"
            surnameError -> "Surname cannot be empty"
            email.isBlank() -> "Email cannot be empty"
            emailError -> "Invalid email format"
            password.isBlank() -> "Password cannot be empty"
            passwordError -> "Password must have at least 6 characters"
            confirmPassword.isBlank() -> "Confirm password is required"
            confirmPasswordError -> "Passwords do not match"
            else -> null
        }

        return validationMessage == null
    }

    /** ✅ BACKEND MESSAGE */
    val backendMessage = when (registerState) {
        is UiState.Error -> (registerState as UiState.Error).message   // np. "Email already taken"
        else -> null
    }

    /** ✅ PRIORYTET: backend > frontend */
    val displayedErrorMessage = backendMessage ?: validationMessage

    /** ✅ Nawigacja po success */
    LaunchedEffect(registerState) {
        if (registerState is UiState.Success) {
            navController.navigate("LoginView") {
                popUpTo("RegisterView") { inclusive = true }
            }
        }
    }

    RegisterFormContent(
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
        confirmPassword = confirmPassword,
        onConfirmPasswordChange = {
            confirmPassword = it
            confirmPasswordError = false
            validationMessage = null
        },
        name = name,
        onNameChange = {
            name = it
            nameError = false
            validationMessage = null
        },
        surname = surname,
        onSurnameChange = {
            surname = it
            surnameError = false
            validationMessage = null
        },

        emailError = emailError,
        passwordError = passwordError,
        confirmPasswordError = confirmPasswordError,
        nameError = nameError,
        surnameError = surnameError,
        errorMessage = displayedErrorMessage,

        onRegisterClick = {
            if (validateRegisterFrontend()) {
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
                width = 300.dp,
                onValueChange = onNameChange,
            )

            Spacer(modifier = Modifier.height(15.dp))

            TextInput(
                value = surname,
                label = "Surname",
                isError = surnameError,
                width = 300.dp,
                onValueChange = onSurnameChange
            )

            Spacer(modifier = Modifier.height(15.dp))

            TextInput(
                value = email,
                label = "Email",
                isError = emailError,
                width = 300.dp,
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

            DisplayButton("Register", onClick = onRegisterClick, modifier = Modifier.fillMaxWidth(0.5f))
        }
    }
}
