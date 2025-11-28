package study.snacktrackmobile.presentation.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import study.snacktrackmobile.viewmodel.ProfileViewModel
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.R

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onEditBodyParameters: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val user by viewModel.user.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showSuccessDialog by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    val montserratFont = FontFamily(Font(R.font.montserrat))
    val scrollState = rememberScrollState()
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { pickedUri ->
            scope.launch {
                val token = TokenStorage.getToken(context)
                if (token != null) {
                    viewModel.uploadImage(token, pickedUri, context.contentResolver)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val token = TokenStorage.getToken(context)
        if (token != null) {
            viewModel.loadProfile(token)
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF2E7D32))
        }
        return
    }

    if (error != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error ?: "", color = Color.Red, fontFamily = montserratFont)
        }
        return
    }

    user?.let { profile ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileAvatar(
                        imageUrl = profile.imageUrl,
                        imagePicker = imagePicker
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${profile.name} ${profile.surname}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            fontFamily = montserratFont,
                            color = Color.Black
                        )
                        Text(
                            profile.email,
                            color = Color.Gray,
                            fontFamily = montserratFont,
                            fontSize = 14.sp
                        )
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        InfoRow(label = "Status", value = profile.status, fontFamily = montserratFont, valueColor = Color.Black)
                        InfoRow(label = "Streak", value = "${profile.streak} 🔥", fontFamily = montserratFont, valueColor = Color.Black)
                        InfoRow(
                            label = "Premium expires",
                            value = profile.premiumExpiration ?: "—",
                            fontFamily = montserratFont,
                            valueColor = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showPasswordDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Password", color = Color.White, fontFamily = montserratFont)
                    }

                    Button(
                        onClick = onEditBodyParameters,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit Body Parameters", color = Color.White, fontFamily = montserratFont)
                    }
                }
            }
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { newPassword ->
                scope.launch {
                    val token = TokenStorage.getToken(context)
                    if (token != null) {
                        val response = viewModel.changePassword(token, newPassword)
                        if (response.isSuccessful) {
                            showSuccessDialog = true
                        } else {
                            localError = response.errorBody()?.string() ?: "Unknown error"
                        }
                    }
                    showPasswordDialog = false
                }
            }
        )
    }
    if (showSuccessDialog) {
        SuccessDialog(
            message = "Password changed successfully!",
            onDismiss = { showSuccessDialog = false }
        )
    }
    if (localError != null) {
        AlertDialog(
            onDismissRequest = { localError = null },
            title = { Text("Error") },
            text = { Text(localError ?: "") },
            confirmButton = {
                TextButton(onClick = { localError = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String, fontFamily: FontFamily, valueColor: Color = Color.Black) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium, fontFamily = fontFamily, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontFamily = fontFamily, color = valueColor, fontSize = 14.sp)
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Change Password", color = Color.Black) },
        text = {
            Column {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        newPasswordError = null
                    },
                    label = { Text("New Password") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(icon, contentDescription = null)
                        }
                    },
                    isError = newPasswordError != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color(0xFF2E7D32),
                        focusedBorderColor = Color(0xFF2E7D32),
                        focusedLabelColor = Color(0xFF2E7D32)
                    )
                )

                if (newPasswordError != null) {
                    Text(
                        text = newPasswordError!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = null
                    },
                    label = { Text("Confirm New Password") },
                    singleLine = true,
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(icon, contentDescription = null)
                        }
                    },
                    isError = confirmPasswordError != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color(0xFF2E7D32),
                        focusedBorderColor = Color(0xFF2E7D32),
                        focusedLabelColor = Color(0xFF2E7D32)
                    )
                )

                if (confirmPasswordError != null) {
                    Text(
                        text = confirmPasswordError!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                var valid = true
                if (newPassword.isBlank()) {
                    newPasswordError = "Password cannot be empty"
                    valid = false
                }
                if (confirmPassword.isBlank()) {
                    confirmPasswordError = "Please confirm your password"
                    valid = false
                }
                if (newPassword != confirmPassword) {
                    confirmPasswordError = "Passwords do not match"
                    valid = false
                }
                if (valid) onConfirm(newPassword)
            }) {
                Text("Save", color = Color(0xFF2E7D32))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun SuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Success", color = Color.Black) },
        text = { Text(message, color = Color.Black) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = Color(0xFF2E7D32))
            }
        }
    )
}