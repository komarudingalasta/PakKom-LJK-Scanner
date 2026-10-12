plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "id.pakkom.ljk"
    compileSdk = 35
    defaultConfig {
        applicationId = "id.pakkom.ljk"
        minSdk = 28
        targetSdk = 35
        versionCode = 6
        versionName = "1.3.1"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies { implementation("androidx.core:core:1.13.1") }
