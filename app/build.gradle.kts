plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.example.xoxo_compose"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.xoxo_compose"
        minSdk = 29
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    // ✅ FIX: Prevent duplicate class crash from org.json vs Android built-in
    configurations.all {
        resolutionStrategy {
            force("org.json:json:20231013")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
    implementation(libs.lottie.compose)
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // Ktor Client
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.ktor:ktor-client-auth:2.3.7")
    implementation("io.ktor:ktor-client-logging:2.3.7")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation(libs.androidx.compose.foundation.layout)

    // ✅ FIX: Correct artifact name is "socket.io-client" (NOT "socket.io-client-java")
    // ✅ FIX: engine.io-client is bundled inside socket.io-client — do NOT add it separately
    // ✅ FIX: Exclude org.json to avoid conflict with Android's built-in JSON classes
    implementation("io.socket:socket.io-client:2.1.1") {
        exclude(group = "org.json", module = "json")
    }

    // ✅ REMOVED: implementation("io.socket:socket.io-client-java:2.1.1")  → wrong artifact name
    // ✅ REMOVED: implementation("io.socket:engine.io-client:2.1.1")       → bundled, causes duplicate
    // ✅ REMOVED: implementation("org.json:json:20231013")                  → conflicts with Android built-in

    implementation(libs.identity.doctypes.jvm)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}