package study.snacktrackmobile.presentation.ui.views

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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

@Composable
fun LoginView(
    navController: NavController,
    viewModel: UserViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val loginState by viewModel.loginState.collectAsState()

    fun validateLogin(): Boolean {
        val emailTrimmed = email.trim()
        val passwordTrimmed = password.trim()

        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()
        val isPasswordValid = passwordTrimmed.length >= 6

        emailError = !isEmailValid
        passwordError = !isPasswordValid

        return isEmailValid && isPasswordValid
    }

    // Obsługa stanu loginu
    when(loginState) {
        is UiState.Loading -> {
            // pokaż np. ProgressBar
        }
        is UiState.Success -> {
            navController.navigate("MainView") {
                popUpTo("LoginView") { inclusive = true }
            }
        }
        is UiState.Error -> {
            // pokaż error
        }
        is UiState.Idle -> {
            // nic nie rób albo pokaż pusty ekran
        }
    }

    LoginFormContent(
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        emailError = emailError,
        passwordError = passwordError,
        showErrorMessage = loginState is UiState.Error && emailError.not() && passwordError.not(),
        onLoginClick = {
            if (validateLogin()) {
                viewModel.login(email.trim(), password.trim())

            } else {
                // tutaj błędy walidacji
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
    showErrorMessage: Boolean,
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

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email", fontFamily = montserratFont, fontSize = 16.sp, color = Color.Black) },
                placeholder = { Text("name@example.com", fontFamily = montserratFont, fontSize = 16.sp) },
                singleLine = true,
                modifier = Modifier
                    .width(300.dp)
                    .background(Color(0xffffffff), shape = RoundedCornerShape(12.dp)),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                textStyle = TextStyle(fontSize = 18.sp, fontFamily = montserratFont, color = Color.Black),
                shape = RoundedCornerShape(12.dp),
                isError = emailError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (emailError) Color.Red else Color.Black,
                    unfocusedBorderColor = if (emailError) Color.Red else Color.Gray,
                )
            )

            Spacer(modifier = Modifier.height(15.dp))

            PasswordInput(
                value = password,
                label = "Password",
                onValueChange = onPasswordChange,
                isError = passwordError
            )

            Spacer(modifier = Modifier.height(15.dp))

            if (showErrorMessage) {
                Text(
                    text = "Enter valid data",
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
