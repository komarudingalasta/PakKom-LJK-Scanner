# PakKom LJK Scanner Android v1.0.1

Aplikasi Android native berbasis **Kotlin**, offline, tanpa Firebase. Versi ini adalah **generator PDF LJK 50 soal A–E**, **belum scanner kamera**.

## Fitur saat ini
- PDF A4 **LJK Personal** (nama, NIS, kelas, kode ujian, paket, QR).
- PDF A4 **LJK Umum** (isian nama, NIS 8 digit, paket A–D).
- Penanda registrasi di empat sudut; peta koordinat `app/src/main/assets/omr_coordinates_v1.json`.
- Menyimpan PDF melalui dialog penyimpanan Android, tanpa izin storage luas.
- Offline; tidak ada login, Firebase, analytics, atau pengiriman data.

## Cara paling mudah membuat APK via GitHub
1. Buat repository GitHub, cabang `main`.
2. **Upload seluruh isi folder ini ke akar repository** (folder `app`, `.github`, `tools`, file `build.gradle.kts`, dll.). Jangan upload ZIP tanpa diekstrak; jangan masukkan folder induk ekstra.
3. Buka **Actions → Build Android APK → Run workflow**.
4. Jika build sukses, buka run → **Artifacts → PakKom-LJK-Scanner-v1.0.1-debug**.
5. Ekstrak hasil unduhan artifacts untuk memperoleh `app-debug.apk`; pasang pada HP Android untuk pengujian.

**Penting:** Ini menggunakan Gradle terpasang oleh workflow GitHub Actions, sehingga tidak memerlukan `gradlew`/wrapper. Jika ingin membangun lokal melalui Android Studio, buka folder proyek dan gunakan Gradle 8.7 + JDK 17 + Android SDK 35.

## Persyaratan dan batasan
- Android 9+ (`minSdk=28`), `compileSdk=35`.
- Kotlin 2.0.21, Android Gradle Plugin 8.6.1, Gradle 8.7, Java 17.
- ZXing core 3.5.3 untuk QR Code saat PDF dibuat.
- **Belum teruji build APK pada lingkungan pembuat source**; workflow disertakan untuk memverifikasi hasil build di GitHub.
- Template perlu diuji pada printer nyata dengan skala 100% sebelum dijadikan standar OMR.
- NIS LJK Umum adalah **8 digit**; kebutuhan NIS berbeda panjang perlu revisi template.
- APK dari Actions adalah **debug**, bukan release bertanda tangan untuk distribusi publik.

## Selanjutnya
v1.1: CameraX, OpenCV, deteksi empat marker, koreksi perspektif, dan pembacaan foto statis. v1.2: live scanning.
