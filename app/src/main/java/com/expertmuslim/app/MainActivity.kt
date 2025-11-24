package com.expertmuslim.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.expertmuslim.app.activities.ArahKiblatActivity
import com.expertmuslim.app.activities.DaftarDoaActivity // <-- IMPORT DITAMBAHKAN
import com.expertmuslim.app.activities.JadwalSholatActivity
import com.expertmuslim.app.activities.LoginActivity
import com.expertmuslim.app.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvUsername: TextView
    private lateinit var ivLogout: ImageView
    private lateinit var cardJadwalSholat: CardView
    private lateinit var cardKiblat: CardView
    private lateinit var cardDoaDzikir: CardView

    // Deklarasi Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
// ### PERBAIKAN: Tambahkan URL Database dan getReference secara terpisah ###
        val databaseUrl = "https://expert-muslim-app-default-rtdb.asia-southeast1.firebasedatabase.app"
        database = FirebaseDatabase.getInstance(databaseUrl).getReference("users")


        // Cek status login
        if (auth.currentUser == null) {
            goToLoginActivity()
            return
        }

        initViews()
        loadUserData()
        setupClickListeners()
    }

    private fun initViews() {
        tvUsername = findViewById(R.id.tvUsername)
        ivLogout = findViewById(R.id.ivLogout)
        cardJadwalSholat = findViewById(R.id.cardJadwalSholat)
        cardKiblat = findViewById(R.id.cardKiblat)
        cardDoaDzikir = findViewById(R.id.cardDoaDzikir)
    }

    private fun setupClickListeners() {
        // Listener untuk tombol Logout
        ivLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Listener untuk Card Jadwal Sholat
        cardJadwalSholat.setOnClickListener {
            startActivity(Intent(this, JadwalSholatActivity::class.java))
        }

        // Listener untuk Card Arah Kiblat
        cardKiblat.setOnClickListener {
            startActivity(Intent(this, ArahKiblatActivity::class.java))
        }

        // ======================================================
        // ### BAGIAN INI YANG DIPERBAIKI ###
        // ======================================================
        // Listener untuk Card Doa & Dzikir
        cardDoaDzikir.setOnClickListener {
            startActivity(Intent(this, DaftarDoaActivity::class.java))
        }
        // ======================================================
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            logout()
            return
        }

        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null && !user.nama.isNullOrEmpty()) {
                    tvUsername.text = user.nama
                } else {
                    tvUsername.text = "Pengguna"
                    Log.w("MainActivity", "Data nama untuk user $userId tidak ditemukan di database.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_LONG).show()
                tvUsername.text = "Pengguna"
                Log.e("MainActivity", "Firebase onCancelled: ${error.message}")
            }
        })
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Keluar")
            .setMessage("Anda yakin ingin keluar dari akun ini?")
            .setPositiveButton("Ya, Keluar") { dialog, _ ->
                logout()
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun logout() {
        auth.signOut()
        Toast.makeText(this, "Berhasil keluar", Toast.LENGTH_SHORT).show()
        goToLoginActivity()
    }

    private fun goToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
