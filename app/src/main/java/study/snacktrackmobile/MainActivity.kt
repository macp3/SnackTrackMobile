package study.snacktrackmobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.messaging.FirebaseMessaging
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.services.MyFirebaseMessagingService
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.theme.SnackTrackMobileTheme
import study.snacktrackmobile.presentation.ui.views.SnackTrackApp

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("FCM", "Notification permission granted")
                fetchAndSendFCMToken()
            } else {
                Log.w("FCM", "User denied notification permission")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Logika powiadomień
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    fetchAndSendFCMToken()
                }
                else -> {
                    requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            fetchAndSendFCMToken()
        }

        // Jedyne wywołanie setContent - tu uruchamiamy aplikację
        setContent {
            SnackTrackMobileTheme {
                SnackTrackApp()
            }
        }
    }

    private fun fetchAndSendFCMToken() {
        CoroutineScope(Dispatchers.IO).launch {
            val jwt = TokenStorage.getToken(applicationContext)
            if (jwt.isNullOrBlank()) {
                Log.w("FCM", "JWT not available yet — skipping FCM token send.")
                return@launch
            }

            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM", "Fetched FCM token: $token")
                    MyFirebaseMessagingService().sendTokenToServer(token)
                } else {
                    Log.e("FCM", "Token fetch failed", task.exception)
                }
            }
        }
    }
}
