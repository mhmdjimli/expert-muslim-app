package com.expertmuslim.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.expertmuslim.app.MainActivity
import com.expertmuslim.app.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvError: TextView
    private lateinit var tvRegister: TextView
    private lateinit var tvForgotPassword: TextView // Kita tambahkan ini
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Cek jika user sudah login, langsung ke MainActivity
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Set layout
        setContentView(R.layout.activity_login)

        // Inisialisasi semua View
        initViews()

        // Setup listener untuk semua tombol
        setupClickListeners()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvError = findViewById(R.id.tvError)
        tvRegister = findViewById(R.id.tvRegister)
        tvForgotPassword = findViewById(R.id.tvForgotPassword) // Inisialisasi Lupa Password
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        // Listener untuk tombol Login
        btnLogin.setOnClickListener {
            loginUser()
        }

        // Listener untuk link ke Register
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Listener untuk Lupa Password (menampilkan Toast untuk saat ini)
        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Fitur ini akan segera tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validasi Input
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

        hideError()
        showLoading(true)

        // Proses Login ke Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } else {
                    val errorMessage = when {
                        task.exception?.message?.contains("user record") == true -> "Email belum terdaftar"
                        task.exception?.message?.contains("password is invalid") == true -> "Password salah"
                        task.exception?.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> "Password salah"
                        else -> "Login gagal, periksa kembali email dan password Anda."
                    }
                    showError(errorMessage)
                }
            }
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun hideError() {
        tvError.visibility = View.GONE
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !show
        btnLogin.alpha = if (show) 0.5f else 1f
    }
}
