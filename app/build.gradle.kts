plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.moonx.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.moonx.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Untuk file besar dari Play Store → relay → VPS
        multiDexEnabled = true
    }

    signingConfigs {
        create("release") {
            storeFile = file("../keystore.jks")
            storePassword = "passwordmu"
            keyAlias = "moonx"
            keyPassword = "passwordmu"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        viewBinding = true // kamu pakai XML layout
    }

    packaging {
        resources.excludes += "META-INF/*"
    }
}

dependencies {
    // AndroidX basic
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Google Auth
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // WorkManager untuk relay background
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Networking relay → VPS backend
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // File streaming
    implementation("androidx.documentfile:documentfile:1.0.1")

    // Play Integrity (cara aman untuk cek PlayStore purchases)
    implementation("com.google.android.play:integrity:1.3.0")

    // Disable Compose (karena kamu pakai XML)
    implementation("androidx.multidex:multidex:2.0.1")
}
