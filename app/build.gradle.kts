plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.com.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.track_assist"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.track_assist"
        minSdk = 23
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.lottie)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase dependencies using BOM
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.analytics)
    implementation(libs.google.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.google.firebase.firestore)
    implementation(libs.firebase.storage)

    // Google Play Services and Places API
    implementation(libs.play.services.location)
    implementation(libs.gms.play.services.maps)
    implementation(libs.places)

    // RecyclerView
    implementation(libs.recyclerview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
