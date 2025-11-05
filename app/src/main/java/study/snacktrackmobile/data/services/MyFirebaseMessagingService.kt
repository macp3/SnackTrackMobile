package study.snacktrackmobile.data.services

import android.app.NotificationChannel
import android.app.NotificationManager
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
import study.snacktrackmobile.data.model.Notification
import study.snacktrackmobile.data.repository.NotificationsRepository
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "SnackTrack"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: ""

        showNotification(title, body)

        Log.d("FCM", "messageArrived");

        NotificationsRepository.addNotification(
            Notification(
                id = System.currentTimeMillis().toString(),
                title = title,
                body = body
            )
        )
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendTokenToServer(token)
    }

    fun sendTokenToServer(token: String) {
        Log.d("FCM", "sending token to server: $token")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject()
                json.put("token", token)
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = json.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/users/device-token")
                    .post(body)
                    .addHeader(
                        "Authorization",
                        "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYWNpZWoucGlldHJhczEyM0BnbWFpbC5jb20iLCJ0eXBlIjoiVVNFUiIsImlhdCI6MTc2MjI2MjQyNSwiZXhwIjoxNzYyNTIxNjI1fQ.JrHYl8VnUUKU9DsWjIJPbszC9Pc5tV4doOinXtlJ07M"
                    )
                    .build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d("FCM", "Device token sent successfully")
                } else {
                    Log.e("FCM", "Failed to send device token: ${response.message}")
                }

            } catch (e: Exception) {
                Log.e("FCM", "Error sending token", e)
            }
        }
    }


    private fun showNotification(title: String, body: String) {
        val channelId = "snacktrack_channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SnackTrack", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
