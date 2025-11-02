package study.snacktrackmobile.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import study.snacktrackmobile.R
import study.snacktrackmobile.model.NotificationItem
import study.snacktrackmobile.repository.NotificationsRepository
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "SnackTrack"
        val body = remoteMessage.notification?.body ?: ""

        // Wyświetlenie lokalnej notyfikacji
        showNotification(title, body)

        // Dodanie do dynamicznej listy (np. przez singleton)
        NotificationsRepository.addNotification(
            NotificationItem(
                id = System.currentTimeMillis().toString(),
                title = title,
                body = body
            )
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // Wyślij token do backendu
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        // Używamy CoroutineScope, aby wysyłać request w tle
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject()
                json.put("token", token)
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = json.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("https://YOUR_BACKEND_URL/device-token") // <-- zmień na swój endpoint
                    .post(body)
                    .addHeader("Authorization", "Bearer YOUR_JWT_TOKEN") // <-- token JWT użytkownika
                    .build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    println("Device token sent successfully")
                } else {
                    println("Failed to send device token: ${response.message}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "snacktrack_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SnackTrack", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
