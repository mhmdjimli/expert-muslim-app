package com.expertmuslim.app.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context    import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.expertmuslim.app.R
import com.google.android.gms.location.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class ArahKiblatActivity : AppCompatActivity(), SensorEventListener {

    // Views
    private lateinit var ivCompassBase: ImageView
    private lateinit var ivQiblaArrow: ImageView
    private lateinit var tvLokasiKiblat: TextView
    private lateinit var tvDerajatKiblat: TextView

    // Sensor Manager
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    // Data Sensor
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var currentAzimuth = 0f

    // Data Lokasi
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: Location? = null

    // Kontrak izin lokasi
    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                fetchCurrentLocation()
            } else {
                Toast.makeText(this, "Izin lokasi ditolak, fitur Kiblat tidak dapat berfungsi.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arah_kiblat)

        setupToolbar()
        initViews()
        initSensorsAndLocation()
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initViews() {
        ivCompassBase = findViewById(R.id.ivCompassBase)
        ivQiblaArrow = findViewById(R.id.ivQiblaArrow)
        tvLokasiKiblat = findViewById(R.id.tvLokasiKiblat)
        tvDerajatKiblat = findViewById(R.id.tvDerajatKiblat)
    }

    private fun initSensorsAndLocation() {
        // Inisialisasi Sensor Manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // Inisialisasi Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onResume() {
        super.onResume()
        // Daftarkan listener sensor
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
        // Cek izin dan mulai ambil lokasi
        checkAndRequestLocationPermission()
    }

    override fun onPause() {
        super.onPause()
        // Hentikan listener sensor untuk menghemat baterai
        sensorManager.unregisterListener(this)
        // Hentikan update lokasi
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Fungsi ini akan dipanggil setiap kali ada perubahan data sensor
    override fun onSensorChanged(event: SensorEvent?) {
        val alpha = 0.97f
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // Low-pass filter untuk accelerometer
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
        }
        if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            // Low-pass filter untuk magnetometer
            geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0]
            geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1]
            geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2]
        }

        // Hitung orientasi
        val rotationMatrix = FloatArray(9)
        val inclinationMatrix = FloatArray(9)
        val isSuccess = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravity, geomagnetic)
        if (isSuccess) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            // 'orientation[0]' adalah azimuth (dalam radian), kita konversi ke derajat
            val azimuthInRadians = orientation[0]
            val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()

            // Hitung arah Kiblat jika lokasi sudah didapat
            userLocation?.let {
                val qiblaDirection = calculateQiblaDirection(it)
                // Panggil fungsi untuk memutar kompas dan panah
                adjustCompassAndArrow(azimuthInDegrees, qiblaDirection)
            }
        }
    }

    private fun adjustCompassAndArrow(azimuth: Float, qiblaDirection: Float) {
        // Animasi untuk memutar dasar kompas agar selalu menunjuk ke Utara
        val compassRotateAnimation = RotateAnimation(
            -currentAzimuth, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 500
            fillAfter = true
        }
        ivCompassBase.startAnimation(compassRotateAnimation)

        // Animasi untuk memutar panah agar menunjuk ke arah Kiblat
        // Sudut rotasi adalah sudut Kiblat dikurangi sudut hadap HP (azimuth)
        val arrowRotateAnimation = RotateAnimation(
            -currentAzimuth + qiblaDirection, -azimuth + qiblaDirection, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 500
            fillAfter = true
        }
        ivQiblaArrow.startAnimation(arrowRotateAnimation)

        currentAzimuth = azimuth
    }

    // Fungsi perhitungan sudut Kiblat
    private fun calculateQiblaDirection(location: Location): Float {
        val kaabaLat = 21.422487
        val kaabaLng = 39.826206
        val userLat = location.latitude
        val userLng = location.longitude

        val lngDiff = Math.toRadians(kaabaLng - userLng)
        val lat1 = Math.toRadians(userLat)
        val lat2 = Math.toRadians(kaabaLat)

        val y = sin(lngDiff) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(lngDiff)

        var qibla = (Math.toDegrees(atan2(y, x)) + 360) % 360

        // Tampilkan derajat di UI
        tvDerajatKiblat.text = "${qibla.toInt()}°"
        return qibla.toFloat()
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
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        tvLokasiKiblat.text = "Mendeteksi lokasi Anda..."
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            userLocation = locationResult.lastLocation
            userLocation?.let {
                tvLokasiKiblat.text = "Lokasi ditemukan, kompas dikalibrasi."
                // Saat lokasi pertama kali didapat, kita langsung hitung sekali
                // agar UI terupdate bahkan sebelum sensor bergerak.
                val qiblaDirection = calculateQiblaDirection(it)
                adjustCompassAndArrow(currentAzimuth, qiblaDirection)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Bisa digunakan untuk menampilkan pesan kalibrasi jika akurasi rendah
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
