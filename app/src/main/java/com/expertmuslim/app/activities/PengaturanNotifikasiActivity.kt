package com.expertmuslim.app.activities

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout // Import RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.expertmuslim.app.R
import com.google.android.material.switchmaterial.SwitchMaterial

class PengaturanNotifikasiActivity : AppCompatActivity() {

    // Views
    private lateinit var switchNotifikasi: SwitchMaterial
    private lateinit var rgSuaraAdzan: RadioGroup
    private lateinit var ivPlayStandar: ImageView
    private lateinit var ivPlayMakkah: ImageView
    private lateinit var ivPlayMadinah: ImageView

    // ### TAMBAHAN BARU ###
    // Referensi ke RadioButton untuk dikontrol manual
    private lateinit var rbAdzanStandar: RadioButton
    private lateinit var rbAdzanMakkah: RadioButton
    private lateinit var rbAdzanMadinah: RadioButton

    // MediaPlayer untuk pratinjau
    private var mediaPlayer: MediaPlayer? = null
    private var currentlyPlaying: ImageView? = null

    companion object {
        const val PREFS_NAME = "ExpertMuslimPrefs"
        const val KEY_NOTIFIKASI_ENABLED = "notifikasi_enabled"
        const val KEY_SUARA_ADZAN = "suara_adzan_pilihan"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengaturan_notifikasi)

        setupToolbar()
        initViews()
        loadSettings()
        setupListeners()
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initViews() {
        switchNotifikasi = findViewById(R.id.switchNotifikasi)
        rgSuaraAdzan = findViewById(R.id.rgSuaraAdzan)
        ivPlayStandar = findViewById(R.id.ivPlayStandar)
        ivPlayMakkah = findViewById(R.id.ivPlayMakkah)
        ivPlayMadinah = findViewById(R.id.ivPlayMadinah)

        // ### TAMBAHAN BARU ###
        // Inisialisasi semua RadioButton
        rbAdzanStandar = findViewById(R.id.rbAdzanStandar)
        rbAdzanMakkah = findViewById(R.id.rbAdzanMakkah)
        rbAdzanMadinah = findViewById(R.id.rbAdzanMadinah)
    }

    private fun setupListeners() {
        // Listener untuk switch ON/OFF
        switchNotifikasi.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationStatus(isChecked)
            updateRadioGroupState(isChecked)
            if (!isChecked) {
                stopSound() // Hentikan suara jika notifikasi dimatikan
            }
        }

        // ### PERBAIKAN UTAMA ###
        // Kita tidak lagi menggunakan setOnCheckedChangeListener untuk RadioGroup.
        // Sebagai gantinya, kita beri listener pada setiap RadioButton atau layout pembungkusnya.

        // Listener untuk setiap baris pilihan suara
        findViewById<RelativeLayout>(R.id.rlAdzanStandar).setOnClickListener { selectSuaraAdzan("adzan_standar") }
        findViewById<RelativeLayout>(R.id.rlAdzanMakkah).setOnClickListener { selectSuaraAdzan("adzan_makkah") }
        findViewById<RelativeLayout>(R.id.rlAdzanMadinah).setOnClickListener { selectSuaraAdzan("adzan_madinah") }

        // Listener untuk tombol Play/Stop (tidak berubah)
        ivPlayStandar.setOnClickListener { playSound(R.raw.adzan_standar, ivPlayStandar) }
        ivPlayMakkah.setOnClickListener { playSound(R.raw.adzan_makkah, ivPlayMakkah) }
        ivPlayMadinah.setOnClickListener { playSound(R.raw.adzan_madinah, ivPlayMadinah) }
    }

    // ### FUNGSI BARU UNTUK MENGONTROL PEMILIHAN ###
    private fun selectSuaraAdzan(suaraTerpilih: String) {
        // Matikan semua radio button terlebih dahulu
        rbAdzanStandar.isChecked = false
        rbAdzanMakkah.isChecked = false
        rbAdzanMadinah.isChecked = false

        // Nyalakan radio button yang sesuai dan simpan pilihan
        when (suaraTerpilih) {
            "adzan_makkah" -> {
                rbAdzanMakkah.isChecked = true
                saveSuaraAdzan("adzan_makkah")
            }
            "adzan_madinah" -> {
                rbAdzanMadinah.isChecked = true
                saveSuaraAdzan("adzan_madinah")
            }
            else -> { // Default ke standar
                rbAdzanStandar.isChecked = true
                saveSuaraAdzan("adzan_standar")
            }
        }
        Toast.makeText(this, "Suara Adzan diubah", Toast.LENGTH_SHORT).show()
    }


    private fun playSound(soundResId: Int, clickedButton: ImageView) {
        if (mediaPlayer?.isPlaying == true) {
            if (currentlyPlaying == clickedButton) {
                stopSound()
            } else {
                stopSound()
                startSound(soundResId, clickedButton)
            }
        } else {
            startSound(soundResId, clickedButton)
        }
    }

    private fun startSound(soundResId: Int, button: ImageView) {
        // Pastikan notifikasi diaktifkan untuk bisa preview
        if (!switchNotifikasi.isChecked) {
            Toast.makeText(this, "Aktifkan notifikasi terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        mediaPlayer = MediaPlayer.create(this, soundResId)
        mediaPlayer?.setOnCompletionListener {
            stopSound()
        }
        mediaPlayer?.start()
        button.setImageResource(R.drawable.ic_stop)
        currentlyPlaying = button
    }

    private fun stopSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        ivPlayStandar.setImageResource(R.drawable.ic_play_arrow)
        ivPlayMakkah.setImageResource(R.drawable.ic_play_arrow)
        ivPlayMadinah.setImageResource(R.drawable.ic_play_arrow)
        currentlyPlaying = null
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val notifEnabled = prefs.getBoolean(KEY_NOTIFIKASI_ENABLED, false)
        val suaraTerpilih = prefs.getString(KEY_SUARA_ADZAN, "adzan_standar")

        switchNotifikasi.isChecked = notifEnabled
        updateRadioGroupState(notifEnabled)

        // Matikan semua dulu
        rbAdzanStandar.isChecked = false
        rbAdzanMakkah.isChecked = false
        rbAdzanMadinah.isChecked = false

        // Nyalakan yang sesuai
        when (suaraTerpilih) {
            "adzan_makkah" -> rbAdzanMakkah.isChecked = true
            "adzan_madinah" -> rbAdzanMadinah.isChecked = true
            else -> rbAdzanStandar.isChecked = true
        }
    }

    private fun updateRadioGroupState(isEnabled: Boolean) {
        val alpha = if (isEnabled) 1.0f else 0.5f
        // Nonaktifkan klik pada setiap baris
        findViewById<RelativeLayout>(R.id.rlAdzanStandar).isEnabled = isEnabled
        findViewById<RelativeLayout>(R.id.rlAdzanMakkah).isEnabled = isEnabled
        findViewById<RelativeLayout>(R.id.rlAdzanMadinah).isEnabled = isEnabled

        // Redupkan seluruh grup
        rgSuaraAdzan.alpha = alpha
    }

    private fun saveNotificationStatus(isEnabled: Boolean) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_NOTIFIKASI_ENABLED, isEnabled)
            .apply()
    }

    private fun saveSuaraAdzan(suara: String) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SUARA_ADZAN, suara)
            .apply()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onStop() {
        super.onStop()
        stopSound()
    }
}
