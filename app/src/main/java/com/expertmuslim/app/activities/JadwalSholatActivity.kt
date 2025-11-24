package com.expertmuslim.app.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.expertmuslim.app.R
import com.expertmuslim.app.api.RetrofitClient
import com.expertmuslim.app.models.AdzanResponse
import com.expertmuslim.app.utils.AlarmHelper
import com.google.android.gms.location.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class JadwalSholatActivity : AppCompatActivity() {

    // Deklarasi Views
    private lateinit var btnBack: ImageView
    private lateinit var btnRefresh: ImageView // Tombol Refresh
    private lateinit var tvTanggal: TextView
    private lateinit var tvLokasi: TextView
    private lateinit var tvSubuh: TextView
    private lateinit var tvDzuhur: TextView
    private lateinit var tvAshar: TextView
    private lateinit var tvMaghrib: TextView
    private lateinit var tvIsya: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: ScrollView
    private lateinit var tvStatusNotifikasi: TextView
    private lateinit var cardPengaturanNotifikasi: CardView // Card baru

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Variabel untuk menyimpan waktu sholat
    private var waktuSubuh: String = ""
    private var waktuDzuhur: String = ""
    private var waktuAshar: String = ""
    private var waktuMaghrib: String = ""
    private var waktuIsya: String = ""

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                fetchCurrentLocation()
            } else {
                showLocationPermissionDeniedDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jadwal_sholat)

        initViews()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupClickListeners()

        setTanggalHariIni()
        checkAndRequestLocationPermission()
    }

    override fun onResume() {
        super.onResume()
        // Perbarui status notifikasi setiap kali kembali ke halaman ini
        loadNotificationStatus()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnRefresh = findViewById(R.id.btnRefresh)
        tvTanggal = findViewById(R.id.tvTanggal)
        tvLokasi = findViewById(R.id.tvLokasi)
        tvSubuh = findViewById(R.id.tvSubuh)
        tvDzuhur = findViewById(R.id.tvDzuhur)
        tvAshar = findViewById(R.id.tvAshar)
        tvMaghrib = findViewById(R.id.tvMaghrib)
        tvIsya = findViewById(R.id.tvIsya)
        progressBar = findViewById(R.id.progressBar)
        scrollView = findViewById(R.id.scrollView)
        tvStatusNotifikasi = findViewById(R.id.tvStatusNotifikasi)
        cardPengaturanNotifikasi = findViewById(R.id.cardPengaturanNotifikasi)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }

        btnRefresh.setOnClickListener {
            Toast.makeText(this, "Menyegarkan jadwal...", Toast.LENGTH_SHORT).show()
            checkAndRequestLocationPermission()
        }

        cardPengaturanNotifikasi.setOnClickListener {
            startActivity(Intent(this, PengaturanNotifikasiActivity::class.java))
        }
    }

    private fun setTanggalHariIni() {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        tvTanggal.text = dateFormat.format(Date())
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        scrollView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation()
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {
        showLoading(true)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).setMaxUpdates(1).build()
        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                locationResult.lastLocation?.let {
                    getPrayerTimesForLocation(it.latitude, it.longitude)
                } ?: handleLocationError()
            }
        }, Looper.getMainLooper())
    }

    private fun handleLocationError() {
        Toast.makeText(this, "Gagal dapat lokasi. Menggunakan lokasi default Jakarta.", Toast.LENGTH_LONG).show()
        getPrayerTimesForLocation(-6.2088, 106.8456) // Lokasi default Jakarta
    }

    private fun getPrayerTimesForLocation(latitude: Double, longitude: Double) {
        showLoading(true)
        val geocoder = Geocoder(this, Locale.getDefault())
        var cityName = "Jakarta"
        var countryName = "Indonesia"
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                cityName = address.subAdminArea ?: address.locality ?: "Unknown"
                countryName = address.countryName ?: "Indonesia"
            }
        } catch (e: IOException) {
            Log.e("JadwalSholat", "Geocoder gagal: ${e.message}")
        }
        tvLokasi.text = cityName

        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        val tune = "0,2,0,0,0,0,2,0"

        RetrofitClient.apiService.getCalendarByCity(cityName, countryName, 3, month, year, tune).enqueue(object : Callback<AdzanResponse> {
            override fun onResponse(call: Call<AdzanResponse>, response: Response<AdzanResponse>) {
                showLoading(false)
                if (response.isSuccessful && response.body() != null) {
                    val dataForToday = response.body()!!.data.find { it.date.gregorian.day == day.toString() }
                    if (dataForToday != null) {
                        val timings = dataForToday.timings
                        waktuSubuh = formatTime(timings.fajr)
                        waktuDzuhur = formatTime(timings.dhuhr)
                        waktuAshar = formatTime(timings.asr)
                        waktuMaghrib = formatTime(timings.maghrib)
                        waktuIsya = formatTime(timings.isha)

                        tvSubuh.text = waktuSubuh
                        tvDzuhur.text = waktuDzuhur
                        tvAshar.text = waktuAshar
                        tvMaghrib.text = waktuMaghrib
                        tvIsya.text = waktuIsya

                        // Jika notifikasi aktif, setup ulang alarm dengan jadwal baru
                        val prefs = getSharedPreferences(PengaturanNotifikasiActivity.PREFS_NAME, Context.MODE_PRIVATE)
                        if (prefs.getBoolean(PengaturanNotifikasiActivity.KEY_NOTIFIKASI_ENABLED, false)) {
                            enableNotifications()
                        }
                    } else {
                        showErrorToast("Tidak ada data jadwal untuk hari ini.")
                    }
                } else {
                    showErrorToast("Gagal memuat jadwal: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<AdzanResponse>, t: Throwable) {
                showLoading(false)
                showErrorToast("Koneksi bermasalah. Periksa internet Anda.")
            }
        })
    }

    private fun formatTime(time: String): String = time.split(" ")[0]

    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun loadNotificationStatus() {
        val prefs = getSharedPreferences(PengaturanNotifikasiActivity.PREFS_NAME, MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(PengaturanNotifikasiActivity.KEY_NOTIFIKASI_ENABLED, false)
        updateNotificationStatusText(isEnabled)
    }

    private fun updateNotificationStatusText(isEnabled: Boolean) {
        tvStatusNotifikasi.text = if (isEnabled) "Notifikasi Adzan Aktif" else "Notifikasi Adzan Nonaktif"
    }

    private fun enableNotifications() {
        if (waktuSubuh.isNotEmpty()) {
            AlarmHelper.setupAllAlarms(this, waktuSubuh, waktuDzuhur, waktuAshar, waktuMaghrib, waktuIsya)
            Toast.makeText(this, "Alarm Adzan diperbarui", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLocationPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Izin Lokasi Ditolak")
            .setMessage("Aplikasi ini memerlukan izin lokasi untuk menampilkan jadwal sholat yang akurat. Buka pengaturan untuk mengizinkan.")
            .setPositiveButton("Pengaturan") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
                handleLocationError()
            }
            .show()
    }
}
