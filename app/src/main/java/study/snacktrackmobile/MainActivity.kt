package study.snacktrackmobile
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import study.snacktrackmobile.ui.theme.SnackTrackMobileTheme
import study.snacktrackmobile.ui.views.SnackTrackApp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnackTrackMobileTheme {
                SnackTrackApp()
            }
        }
    }
}
