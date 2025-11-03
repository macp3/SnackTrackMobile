package study.snacktrackmobile.presentation.ui.views

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import study.snacktrackmobile.presentation.ui.components.DisplayButton
import study.snacktrackmobile.presentation.ui.components.PasswordInput
import study.snacktrackmobile.presentation.ui.components.SnackTrackTopBar
import study.snacktrackmobile.presentation.ui.components.montserratFont

@Composable
fun RegisterView(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var repeatPasswordError by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }

    fun validateRegister(): Boolean {
        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isPasswordValid = password.length >= 6
        val doPasswordsMatch = password == repeatPassword

        emailError = !isEmailValid
        passwordError = !isPasswordValid
        repeatPasswordError = !doPasswordsMatch

        return isEmailValid && isPasswordValid && doPasswordsMatch
    }

    RegisterFormContent(
        email = email,
        onEmailChange = {
            email = it
            emailError = false
            showErrorMessage = false
        },
        password = password,
        onPasswordChange = {
            password = it
            passwordError = false
            showErrorMessage = false
        },
        repeatPassword = repeatPassword,
        onRepeatPasswordChange = {
            repeatPassword = it
            repeatPasswordError = false
            showErrorMessage = false
        },
        emailError = emailError,
        passwordError = passwordError,
        repeatPasswordError = repeatPasswordError,
        showErrorMessage = showErrorMessage,
        onRegisterClick = {
            if (validateRegister()) {
                navController.navigate("MainView")
            } else {
                showErrorMessage = true
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
    repeatPassword: String,
    onRepeatPasswordChange: (String) -> Unit,
    emailError: Boolean,
    passwordError: Boolean,
    repeatPasswordError: Boolean,
    showErrorMessage: Boolean,
    onRegisterClick: () -> Unit
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
            Text("Start your fit journey now!", fontFamily = montserratFont, fontSize = 36.sp, textAlign = TextAlign.Center, lineHeight = 40.sp)
            Spacer(modifier = Modifier.height(35.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email", fontFamily = montserratFont, fontSize = 16.sp, color = Color.Black) },
                placeholder = { Text("name@example.com", fontFamily = montserratFont, fontSize = 16.sp) },
                singleLine = true,
                modifier = Modifier
                    .width(300.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp)),
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

            PasswordInput(
                value = repeatPassword,
                label = "Repeat password",
                onValueChange = onRepeatPasswordChange,
                isError = repeatPasswordError
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

            DisplayButton("Register", onClick = onRegisterClick)
        }
    }
}


