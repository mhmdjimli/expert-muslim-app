package com.expertmuslim.app.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import java.util.*

object AlarmHelper {

    private const val TAG = "AlarmHelper"

    // Request code harus unik untuk setiap alarm
    private const val SUBUH_REQUEST_CODE = 101
    private const val DZUHUR_REQUEST_CODE = 102
    private const val ASHAR_REQUEST_CODE = 103
    private const val MAGHRIB_REQUEST_CODE = 104
    private const val ISYA_REQUEST_CODE = 105

    // Kunci untuk SharedPreferences
    private const val PREFS_NAME = "ExpertMuslimPrefs"
    private const val KEY_JADWAL_SUBUH = "jadwal_subuh"
    private const val KEY_JADWAL_DZUHUR = "jadwal_dzuhur"
    private const val KEY_JADWAL_ASHAR = "jadwal_ashar"
    private const val KEY_JADWAL_MAGHRIB = "jadwal_maghrib"
    private const val KEY_JADWAL_ISYA = "jadwal_isya"

    fun setupAllAlarms(
        context: Context,
        subuh: String, dzuhur: String, ashar: String, maghrib: String, isya: String
    ) {
        Log.d(TAG, "Mulai mengatur semua alarm...")
        saveJadwalToPreferences(context, subuh, dzuhur, ashar, maghrib, isya)

        setupAlarm(context, "Subuh", subuh, SUBUH_REQUEST_CODE)
        setupAlarm(context, "Dzuhur", dzuhur, DZUHUR_REQUEST_CODE)
        setupAlarm(context, "Ashar", ashar, ASHAR_REQUEST_CODE)
        setupAlarm(context, "Maghrib", maghrib, MAGHRIB_REQUEST_CODE)
        setupAlarm(context, "Isya", isya, ISYA_REQUEST_CODE)
        Log.d(TAG, "Semua alarm telah selesai diatur.")
    }

    private fun saveJadwalToPreferences(
        context: Context,
        subuh: String, dzuhur: String, ashar: String, maghrib: String, isya: String
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_JADWAL_SUBUH, subuh)
            putString(KEY_JADWAL_DZUHUR, dzuhur)
            putString(KEY_JADWAL_ASHAR, ashar)
            putString(KEY_JADWAL_MAGHRIB, maghrib)
            putString(KEY_JADWAL_ISYA, isya)
            apply()
        }
        Log.d(TAG, "Jadwal sholat berhasil disimpan ke SharedPreferences.")
    }

    private fun setupAlarm(context: Context, waktuSholat: String, waktu: String, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AdzanReceiver::class.java).apply {
            putExtra(AdzanReceiver.EXTRA_WAKTU_SHOLAT, waktuSholat)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Parse waktu dari "HH:mm"
        val parts = waktu.split(":")
        if (parts.size != 2) {
            Log.e(TAG, "Format waktu salah untuk $waktuSholat: $waktu")
            return
        }

        val hour = parts[0].toIntOrNull()
        val minute = parts[1].toIntOrNull()

        if (hour == null || minute == null) {
            Log.e(TAG, "Tidak dapat mem-parsing waktu untuk $waktuSholat: $waktu")
            return
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Jika waktu sudah lewat hari ini, set alarm untuk besok
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // Cek izin untuk alarm presisi (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                Log.d(TAG, "Alarm presisi diatur untuk $waktuSholat pada ${calendar.time}")
            } else {
                Log.w(TAG, "Izin alarm presisi ditolak. Fitur notifikasi mungkin tidak akurat.")
                // Opsional: set alarm tidak presisi sebagai fallback
                // alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            Log.d(TAG, "Alarm presisi diatur untuk $waktuSholat pada ${calendar.time}")
        }
    }

    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AdzanReceiver::class.java)

        val requestCodes = listOf(SUBUH_REQUEST_CODE, DZUHUR_REQUEST_CODE, ASHAR_REQUEST_CODE, MAGHRIB_REQUEST_CODE, ISYA_REQUEST_CODE)

        requestCodes.forEach { requestCode ->
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Membatalkan alarm dengan request code: $requestCode")
            }
        }
        Log.d(TAG, "Semua alarm telah selesai dibatalkan.")
    }
}
