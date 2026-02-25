plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.tvstream.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tvstream.app"
        minSdk = 21          // Android 5.0 – supports both TV and Phone
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // Enable ViewBinding – no synthetic imports, no find ViewById
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Leanback – Netflix-style TV UI (BrowseSupportFragment, ImageCardView, etc.)
    implementation(libs.androidx.leanback)

    // Media3 ExoPlayer (replaces old ExoPlayer, fully modern)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)   // HLS (.m3u8) support
    implementation(libs.androidx.media3.ui)              // StyledPlayerView

    // Lifecycle – ViewModel + LiveData/StateFlow
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Activity and Fragment KTX extensions
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Glide – efficient image loading with disk + memory cache
    implementation(libs.glide)

    // Coroutines – for StateFlow / async data loading
    implementation(libs.kotlinx.coroutines.android)
}
