package com.expertmuslim.app.models

// Kita hanya perlu field yang benar-benar disimpan di database
// Jadikan field bisa null dengan '?' untuk keamanan saat data belum ada
data class User(
    val nama: String? = null,
    val email: String? = null
)
