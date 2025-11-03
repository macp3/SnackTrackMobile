package study.snacktrackmobile
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.messaging.FirebaseMessaging
import study.snacktrackmobile.ui.components.BottomNavigationBar
import study.snacktrackmobile.ui.components.SnackTrackTopBarCalendar
import study.snacktrackmobile.ui.theme.SnackTrackMobileTheme
import study.snacktrackmobile.ui.views.InitialSurveyView
import study.snacktrackmobile.ui.views.SnackTrackApp


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
                //SnackTrackApp()
                InitialSurveyView {  }
            }
        }
    }
}
