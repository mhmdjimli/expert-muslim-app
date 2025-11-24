plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Terapkan plugin Google Services di sini
    id("com.google.gms.google-services")
}

android {
    namespace = "com.expertmuslim.app"
    // === PERBAIKAN 1: Gunakan compileSdk yang stabil ===
    compileSdk = 34

    defaultConfig {
        applicationId = "com.expertmuslim.app"
        minSdk = 26
        // === PERBAIKAN 2: Sesuaikan targetSdk dengan compileSdk ===
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // === PERBAIKAN 3: Gunakan versi dependensi yang stabil dan terbukti ===

    // Android Core (versi stabil)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity:1.8.0") // Versi stabil, bukan 1.11.0
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Firebase (versi stabil dari BOM)
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-database")


    // UI & Material Design
    implementation("com.google.android.material:material:1.11.0") // Versi stabil
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Retrofit (versi Anda sudah stabil)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Location Services (versi stabil)
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
