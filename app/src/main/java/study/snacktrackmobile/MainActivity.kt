package study.snacktrackmobile
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import study.snacktrackmobile.ui.theme.SnackTrackMobileTheme
import study.snacktrackmobile.ui.views.LoginScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnackTrackMobileTheme{
                LoginScreen(
                    onLoginClick = {},
                    onRegisterClick = {},
                    onGoogleSignInClick = {}
                )
            }
        }
    }
}
