# Expert Muslim App

Aplikasi Android untuk membantu umat Muslim dalam beribadah dan meningkatkan pengetahuan agama Islam.

## 📱 Fitur Utama

- **Arah Kiblat**: Menentukan arah kiblat menggunakan sensor kompas
- **Daftar Doa**: Kumpulan doa-doa harian dalam Islam
- **Jadwal Sholat**: Menampilkan waktu sholat berdasarkan lokasi
- **Login & Register**: Sistem autentikasi pengguna
- **Pengaturan Notifikasi**: Pengingat waktu sholat

## 🛠️ Teknologi yang Digunakan

- **Platform**: Android (Kotlin)
- **Backend**: Firebase
    - Firebase Authentication
    - Firebase Storage
- **API**: Adzan API untuk jadwal sholat
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit

## 📂 Struktur Project

```
app/
├── src/
│   ├── androidTest/
│   └── main/
│       ├── assets/
│       └── java/com/expertmuslim/app/
│           ├── activities/
│           │   ├── ArahKiblatActivity
│           │   ├── DaftarDoaActivity
│           │   ├── JadwalSholatActivity
│           │   ├── LoginActivity
│           │   ├── PengaturanNotifikasiActivity
│           │   └── RegisterActivity
│           ├── adapters/
│           │   └── DoaDzikirAdapter
│           ├── api/
│           │   ├── AdzanApiService
│           │   └── RetrofitClient
│           └── models/
│               ├── AdzanResponse.kt
│               ├── DoaDzikir
│               ├── JadwalSholat
│               └── User
```

## 🚀 Cara Menjalankan Project

### Prerequisites

- Android Studio (versi terbaru)
- JDK 8 atau lebih tinggi
- Android SDK (API Level 36.1 atau sesuai target)
- Akun Firebase

### Langkah Instalasi

1. Clone repository ini
```bash
git clone [URL_REPOSITORY]
```

2. Buka project dengan Android Studio

3. Setup Firebase:
    - Buat project baru di [Firebase Console](https://console.firebase.google.com)
    - Download file `google-services.json`
    - Letakkan file tersebut di folder `app/`
    - Aktifkan Firebase Authentication dan Firebase Storage

4. Update konfigurasi API:
    - Buka file konfigurasi API
    - Masukkan API key untuk Adzan API jika diperlukan

5. Sync Gradle dan Build project

6. Run aplikasi di emulator atau device fisik

## 🔑 Konfigurasi Firebase

File `google-services.json` harus berisi konfigurasi:
- Project ID: `expert-muslim-55d00`
- Storage Bucket: `expert-muslim-55d00.firebasestorage.app`
- Package Name: `com.expertmuslim.app`

## 📄 License

[Tentukan lisensi yang sesuai]

## 👨‍💻 Developer

[Kelompok 1 Muhammad Jimli Asyidqi-Illiya Faiza-Zhafira Nadia Veranita]

## 📞 Kontak

Untuk pertanyaan atau saran, silakan hubungi:
- Email: [mjimliasidik@gmail.com]
- GitHub: [Exeel21]

## 🙏 Kontribusi

Kontribusi selalu diterima! Silakan buat pull request atau laporkan issue jika menemukan bug.

---

**Catatan**: Pastikan untuk tidak meng-commit file `google-services.json` yang berisi kredensial sensitif ke repository public.