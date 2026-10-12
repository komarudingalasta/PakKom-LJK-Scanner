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
        versionCode = 3
        versionName = "1.1.0"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies { }
