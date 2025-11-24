package com.expertmuslim.app.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.expertmuslim.app.R
import com.expertmuslim.app.models.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    // ... (deklarasi variabel tidak berubah) ...
    private lateinit var etNama: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etKonfirmasiPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvError: TextView
    private lateinit var btnBack: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var tvLogin: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        // ### INI BARIS YANG DIPERBAIKI ###
        database = FirebaseDatabase.getInstance("https://expert-muslim-app-default-rtdb.asia-southeast1.firebasedatabase.app")

        // Inisialisasi Views
        initViews()

        // Setup Listener
        setupClickListeners()
    }

    private fun initViews() {
        etNama = findViewById(R.id.etNama)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etKonfirmasiPassword = findViewById(R.id.etKonfirmasiPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvError = findViewById(R.id.tvError)
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
        tvLogin = findViewById(R.id.tvLogin)
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            registerUser()
        }
        btnBack.setOnClickListener {
            finish()
        }
        tvLogin.setOnClickListener {
            finish()
        }
    }


    // =================================================================
    // ### FUNGSI registerUser() YANG DIOPTIMALKAN ###
    // =================================================================
    private fun registerUser() {
        val nama = etNama.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val konfirmasiPassword = etKonfirmasiPassword.text.toString().trim()

        // ... (Validasi tidak berubah) ...
        if (nama.isEmpty()) {
            showError("Nama tidak boleh kosong")
            etNama.requestFocus()
            return
        }
        if (email.isEmpty()) {
            showError("Email tidak boleh kosong")
            etEmail.requestFocus()
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Format email tidak valid")
            etEmail.requestFocus()
            return
        }
        if (password.isEmpty()) {
            showError("Password tidak boleh kosong")
            etPassword.requestFocus()
            return
        }
        if (password.length < 6) {
            showError("Password minimal 6 karakter")
            etPassword.requestFocus()
            return
        }
        if (password != konfirmasiPassword) {
            showError("Password tidak sama")
            etKonfirmasiPassword.requestFocus()
            return
        }

        hideError()
        showLoading(true)

        // Proses registrasi
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Periksa jika activity masih aktif sebelum melanjutkan
                if (!isFinishing && !isDestroyed) {
                    if (task.isSuccessful) {
                        // **OPTIMISASI 1:** Dapatkan userId, lalu langsung beri respon ke user.
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            saveUserDataToDatabase(userId, nama, email)
                        }

                        showLoading(false)
                        Toast.makeText(this, "Registrasi berhasil! Silakan login", Toast.LENGTH_LONG).show()

                        // **OPTIMISASI 2:** Tutup halaman register SEGERA.
                        // Proses penyimpanan ke database akan terus berjalan di background.
                        finish()

                    } else {
                        showLoading(false)
                        val errorMessage = when {
                            task.exception?.message?.contains("email address is already in use") == true ->
                                "Email sudah terdaftar"
                            task.exception?.message?.contains("network error") == true ->
                                "Gagal terhubung ke server. Periksa koneksi internet Anda."
                            else -> "Registrasi gagal, coba lagi nanti."
                        }
                        showError(errorMessage)
                        Log.e("RegisterActivity", "Registrasi gagal", task.exception)
                    }
                }
            }
    }

    // Fungsi saveUserDataToDatabase tidak perlu diubah, sudah cukup baik.
    private fun saveUserDataToDatabase(userId: String, nama: String, email: String) {
        val user = User(
            nama = nama,
            email = email
        )
        database.reference.child("users").child(userId).setValue(user)
            .addOnSuccessListener {
                // Berhasil menyimpan data, tidak perlu melakukan apa-apa di sini
                // karena user sudah diberi tahu bahwa registrasi berhasil.
                Log.d("RegisterActivity", "Data user berhasil disimpan ke database.")
            }
            .addOnFailureListener { e ->
                // Jika gagal, hanya catat di log. Kegagalan ini tidak boleh
                // mengganggu pengalaman pengguna yang sudah merasa berhasil registrasi.
                Log.e("RegisterActivity", "Gagal menyimpan data user ke database: ${e.message}")
            }
    }

    // ... (fungsi showError, hideError, showLoading tidak berubah) ...
    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun hideError() {
        tvError.visibility = View.GONE
    }



    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !show
        btnRegister.alpha = if (show) 0.5f else 1f
    }
}
