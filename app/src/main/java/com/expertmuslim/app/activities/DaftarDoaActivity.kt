package com.expertmuslim.app.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.expertmuslim.app.R
import com.expertmuslim.app.adapters.DoaDzikirAdapter
import com.expertmuslim.app.models.DoaDzikir
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class DaftarDoaActivity : AppCompatActivity() {

    private lateinit var rvDoaDzikir: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: DoaDzikirAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_doa)

        setupToolbar()
        initViews()
        loadDoaDzikirData()
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        rvDoaDzikir = findViewById(R.id.rvDoaDzikir)
        progressBar = findViewById(R.id.progressBar)
        // Kita tidak perlu mengatur layout manager di sini karena sudah diatur di XML
    }

    private fun loadDoaDzikirData() {
        progressBar.visibility = View.VISIBLE
        rvDoaDzikir.visibility = View.GONE

        try {
            val jsonString = readJsonFromAssets("doa_dzikir_sholat.json")
            if (jsonString != null) {
                val gson = Gson()
                val listType = object : TypeToken<List<DoaDzikir>>() {}.type
                val doaList: List<DoaDzikir> = gson.fromJson(jsonString, listType)

                // Setelah data berhasil di-parse, setup RecyclerView
                setupRecyclerView(doaList)

                progressBar.visibility = View.GONE
                rvDoaDzikir.visibility = View.VISIBLE
            } else {
                handleError("File data tidak ditemukan.")
            }
        } catch (e: Exception) {
            handleError("Gagal memuat data: ${e.message}")
        }
    }

    private fun readJsonFromAssets(fileName: String): String? {
        return try {
            val inputStream = assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: IOException) {
            Log.e("DaftarDoaActivity", "Error reading JSON file", e)
            null
        }
    }

    private fun setupRecyclerView(doaList: List<DoaDzikir>) {
        adapter = DoaDzikirAdapter(doaList)
        rvDoaDzikir.adapter = adapter
    }

    private fun handleError(message: String) {
        progressBar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e("DaftarDoaActivity", message)
    }
}
