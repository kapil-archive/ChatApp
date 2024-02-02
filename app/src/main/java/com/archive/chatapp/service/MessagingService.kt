package com.archive.chatapp.service
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.archive.chatapp.MainActivity
import com.archive.chatapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService(){
    companion object {
        val notificationData = mutableStateOf<String?>(null)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle incoming message here
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        val notificationText = data["text"] ?: "Default Notification Text"
        Log.i("FCM",data.toString())
        // Update the mutable state to trigger recomposition
        notificationData.value = notificationText

        // You may also want to show a local notification using NotificationManager
        // or perform other actions based on the incoming message.
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                // Add any extras or data to the intent
                putExtra("notificationData", notificationText)
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this)
            .setContentTitle("Your App Name")
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.facebook_logo_2023)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 0
        notificationManager.notify(notificationId, notification)
    }
}