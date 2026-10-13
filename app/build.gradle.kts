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
        versionCode = 166
        versionName = "1.6.6"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.activity:activity:1.9.3")
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
}
