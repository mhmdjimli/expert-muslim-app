package com.expertmuslim.app.models

import com.google.gson.annotations.SerializedName

// Model baru ini mewakili array data, bukan objek tunggal
data class AdzanResponse(
    @SerializedName("code")
    val code: Int,    @SerializedName("status")
    val status: String,

    @SerializedName("data")
    val data: List<DailyData> // Sekarang berupa List (Array)
)

data class DailyData(
    @SerializedName("timings")
    val timings: Timings,

    @SerializedName("date")
    val date: DateInfo
)

data class Timings(
    @SerializedName("Fajr")
    val fajr: String,
    @SerializedName("Dhuhr")
    val dhuhr: String,
    @SerializedName("Asr")
    val asr: String,
    @SerializedName("Maghrib")
    val maghrib: String,
    @SerializedName("Isha")
    val isha: String
)

data class DateInfo(
    @SerializedName("gregorian")
    val gregorian: GregorianDate
)

data class GregorianDate(
    @SerializedName("day")
    val day: String
)
