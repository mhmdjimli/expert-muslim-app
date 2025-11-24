package com.expertmuslim.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.expertmuslim.app.R
import com.expertmuslim.app.models.DoaDzikir

class DoaDzikirAdapter(private val doaList: List<DoaDzikir>) :
    RecyclerView.Adapter<DoaDzikirAdapter.DoaViewHolder>() {

    // ViewHolder: Kelas ini memegang referensi ke setiap view di dalam item layout.
    // Ini menghindari pemanggilan findViewById() yang berulang dan mahal.
    class DoaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvArabic: TextView = itemView.findViewById(R.id.tvArabic)
        val tvLatin: TextView = itemView.findViewById(R.id.tvLatin)
        val tvTranslation: TextView = itemView.findViewById(R.id.tvTranslation)
    }

    // Dipanggil saat RecyclerView membutuhkan ViewHolder baru (saat membuat baris baru).
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoaViewHolder {
        // 'Inflate' atau membuat tampilan dari file layout XML item_doa_dzikir.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doa_dzikir, parent, false)
        return DoaViewHolder(view)
    }

    // Mengembalikan jumlah total item dalam daftar data.
    override fun getItemCount(): Int {
        return doaList.size
    }

    // Dipanggil oleh RecyclerView untuk menampilkan data pada posisi tertentu.
    // Metode ini menghubungkan data dari 'doaList' ke view di dalam ViewHolder.
    override fun onBindViewHolder(holder: DoaViewHolder, position: Int) {
        // Dapatkan data untuk posisi saat ini
        val currentItem = doaList[position]

        // Set teks untuk setiap TextView di ViewHolder
        holder.tvTitle.text = currentItem.title
        holder.tvArabic.text = currentItem.arabic
        holder.tvLatin.text = currentItem.latin
        holder.tvTranslation.text = currentItem.translation
    }
}
