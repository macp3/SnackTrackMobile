package study.snacktrackmobile
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.messaging.FirebaseMessaging
import study.snacktrackmobile.presentation.ui.theme.SnackTrackMobileTheme
import study.snacktrackmobile.presentation.ui.views.InitialSurveyView
import study.snacktrackmobile.presentation.ui.views.SnackTrackApp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "FCM token: $token")
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
