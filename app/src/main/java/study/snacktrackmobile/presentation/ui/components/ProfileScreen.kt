package study.snacktrackmobile.presentation.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
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

    val montserratFont = FontFamily(Font(R.font.montserrat))

    var showPasswordDialog by remember { mutableStateOf(false) }

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
        val painter = rememberAsyncImagePainter(profile.imageUrl ?: "")
        val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // TODO: convert Uri → MultipartBody.Part and call viewModel.uploadImage()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .background(Color(0xFFF5F5F5)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Image(
                        painter = painter,
                        contentDescription = "Profile image",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable { imagePicker.launch("image/*") },
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "${profile.name} ${profile.surname}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        fontFamily = montserratFont
                    )
                    Text(profile.email, color = Color.Gray, fontFamily = montserratFont)

                    Spacer(Modifier.height(16.dp))
                    Divider()

                    Spacer(Modifier.height(16.dp))
                    InfoRow(label = "Status", value = profile.status, fontFamily = montserratFont)
                    InfoRow(label = "Streak", value = "${profile.streak} 🔥", fontFamily = montserratFont)
                    InfoRow(label = "Premium expires", value = profile.premiumExpiration ?: "—", fontFamily = montserratFont)

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { showPasswordDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text("Change Password", color = Color.White, fontFamily = montserratFont)
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = onEditBodyParameters,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text("Edit Body Parameters", color = Color.White, fontFamily = montserratFont)
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
                                val error = response.errorBody()?.string()
                                localError = error ?: "Unknown error"
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
}

@Composable
fun InfoRow(label: String, value: String, fontFamily: FontFamily) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium, fontFamily = fontFamily)
        Text(value, fontWeight = FontWeight.Normal, fontFamily = fontFamily)
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
        title = { Text("Change Password") },
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
                    modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.fillMaxWidth()
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
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
        title = { Text("Success") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}


