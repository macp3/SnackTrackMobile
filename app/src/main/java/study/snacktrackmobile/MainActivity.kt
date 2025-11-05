package study.snacktrackmobile
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.messaging.FirebaseMessaging
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import study.snacktrackmobile.data.services.MyFirebaseMessagingService
import study.snacktrackmobile.presentation.ui.components.SnackTrackTopBarWithIcons
import study.snacktrackmobile.presentation.ui.theme.SnackTrackMobileTheme
import study.snacktrackmobile.presentation.ui.views.InitialSurveyView
import study.snacktrackmobile.presentation.ui.views.SnackTrackApp


class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchAndSendFCMToken()
            } else {
                Log.w("FCM", "User denied notification permission")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        setContent {
            SnackTrackMobileTheme {
                //SnackTrackApp()
                SnackTrackTopBarWithIcons()
            }
        }
    }

    private fun fetchAndSendFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "FCM token: $token")
                MyFirebaseMessagingService().sendTokenToServer(token)
            } else {
                Log.e("FCM", "Token fetch failed", task.exception)
            }
        }

        setContent {
            SnackTrackMobileTheme {
                SnackTrackApp()
                //InitialSurveyView {  }
            }
        }
    }
}
