package com.expertmuslim.app.api

import com.expertmuslim.app.models.AdzanResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AdzanApiService {

    // ### PERBAIKAN UTAMA DI SINI ###
    // Menggunakan endpoint /calendarByCity dan menambahkan parameter 'tune'
    // Ini adalah metode yang paling andal dan akurat
    @GET("v1/calendarByCity")
    fun getCalendarByCity(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int,      // Method akan kita set ke 3 (Muslim World League)
        @Query("month") month: Int,
        @Query("year") year: Int,
        @Query("tune") tune: String       // Parameter 'tune' untuk penyesuaian waktu
    ): Call<AdzanResponse>
}
