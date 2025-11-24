package com.expertmuslim.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.expertmuslim.app.MainActivity
import com.expertmuslim.app.R
import com.expertmuslim.app.activities.PengaturanNotifikasiActivity

class AdzanReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AdzanReceiver"
        const val CHANNEL_ID = "adzan_channel"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_WAKTU_SHOLAT = "extra_waktu_sholat"
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val prefs = context.getSharedPreferences(PengaturanNotifikasiActivity.PREFS_NAME, Context.MODE_PRIVATE)
            val isNotifikasiEnabled = prefs.getBoolean(PengaturanNotifikasiActivity.KEY_NOTIFIKASI_ENABLED, true)

            if (!isNotifikasiEnabled) {
                Log.d(TAG, "Notifikasi dinonaktifkan, skip notifikasi")
                return
            }

            val waktuSholat = intent.getStringExtra(EXTRA_WAKTU_SHOLAT) ?: "Sholat"
            Log.d(TAG, "Showing notification for: $waktuSholat")

            // Ambil suara adzan pilihan dari SharedPreferences
            val suaraPilihan = prefs.getString(PengaturanNotifikasiActivity.KEY_SUARA_ADZAN, "adzan_standar")
            val soundResource = when (suaraPilihan) {
                "adzan_makkah" -> R.raw.adzan_makkah
                "adzan_madinah" -> R.raw.adzan_madinah
                else -> R.raw.adzan_standar
            }
            val soundUri = Uri.parse("android.resource://${context.packageName}/$soundResource")

            createNotificationChannel(context, soundUri)
            showNotification(context, waktuSholat)
        } catch (e: Exception) {
            Log.e(TAG, "Error di onReceive: ${e.message}", e)
        }
    }

    private fun createNotificationChannel(context: Context, soundUri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pengingat Adzan"
            val descriptionText = "Notifikasi pengingat waktu sholat 5 waktu"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM) // Usage yang tepat untuk alarm
                    .build()
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, waktuSholat: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val emoji = when (waktuSholat.lowercase()) {
            "subuh" -> "🌅"
            "dzuhur" -> "☀️"
            "ashar" -> "🌤️"
            "maghrib" -> "🌆"
            "isya" -> "🌙"
            else -> "🕌"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$emoji Waktu Sholat $waktuSholat")
            .setContentText("Saatnya menunaikan sholat $waktuSholat.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
