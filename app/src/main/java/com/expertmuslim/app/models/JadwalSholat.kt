package com.expertmuslim.app.models

data class JadwalSholat(
    val tanggal: String = "",
    val subuh: String = "",
    val dzuhur: String = "",
    val ashar: String = "",
    val maghrib: String = "",
    val isya: String = "",
    val lokasi: String = ""
)