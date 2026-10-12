# PakKom LJK Scanner Android v1.0.2

Proyek Android Kotlin untuk membuat PDF LJK Personal dan Umum, masing-masing 50 soal A-E. Ini **belum** memiliki fitur pemindaian kamera/OMR.

## Upload ke GitHub

1. Ekstrak ZIP ini.
2. Hapus workflow lama `.github/workflows/build-apk.yml` di repository jika perlu, kemudian unggah seluruh isi hasil ekstrak ke **root** repository. Pastikan file `.github/workflows/build-apk.yml` ikut terunggah.
3. Commit ke branch `main`.
4. Buka **Actions > Build Android APK** dan jalankan workflow atau tunggu trigger push.
5. Bila sukses, ambil ZIP artifact `PakKom-LJK-Scanner-v1.0.2-debug` dan ekstrak `app-debug.apk` untuk pengujian.

## Perubahan v1.0.2

- Menghapus ketergantungan `android-actions/setup-android@v3` yang sebelumnya gagal memasang paket SDK legacy `tools`.
- Menyiapkan SDK dengan `sdkmanager` yang tersedia di GitHub-hosted runner Ubuntu dan hanya menginstal `platforms;android-35`, `build-tools;35.0.0`, dan `platform-tools`.
- Memperbaiki indentasi workflow YAML.
- Mempertahankan generator PDF, QR Code, dan koordinat LJK dari v1.0.1.

## Catatan

Build APK belum dijalankan di lingkungan ini (tidak ada Android SDK/Gradle runner). Workflow harus diverifikasi lewat GitHub Actions. Source tidak perlu Firebase dan tidak mengirim hasil scan ke server.
