# PakKom LJK Scanner — Android Native v1.0 (Tahap Generator)

Proyek Android Studio (Kotlin) untuk membuat PDF LJK **Personal** dan **Umum**, 50 soal A–E, kertas A4. Tidak memakai Firebase, tidak memerlukan izin internet, dan tidak menyimpan identitas ke server.

## Cara membuka dan membuat APK
1. Instal Android Studio versi yang mendukung Android Gradle Plugin 8.6.1, JDK 17, Android SDK 35.
2. Ekstrak ZIP ini, pilih **Open** dan arahkan ke folder `PakKom-LJK-Android-v1`.
3. Tunggu Gradle Sync; unduhan plugin/dependensi memerlukan internet saat build pertama.
4. Hubungkan HP Android (USB debugging) lalu klik Run, atau pilih **Build > Build APK(s)**.
5. APK debug berada di `app/build/outputs/apk/debug/app-debug.apk` setelah build sukses.

## Fungsi yang sudah diimplementasikan
- Form identitas, pemilihan LJK Personal/Umum dan paket A–D.
- PDF A4 vektor dengan 4 marker sudut, 50 nomor A–E.
- LJK Personal: identitas tercetak dan QR Code.
- LJK Umum: kolom NIS 8 digit dengan bulatan 0–9 serta paket A–D.
- Menyimpan PDF melalui Android Storage Access Framework (pengguna memilih folder).
- Peta koordinat OMR `app/src/main/assets/omr_coordinates_v1.json`, sama dengan prototipe tahap sebelumnya.

## Batasan penting
- **Belum ada kamera, pembaca OMR, input kunci, atau penilaian** pada tahap ini. Fitur tersebut adalah tahap berikutnya.
- **Belum dibuat APK siap instal** karena Android SDK/Gradle build tidak tersedia di lingkungan pembuatan ini; source disediakan untuk build Android Studio/GitHub Actions.
- PDF dan QR harus diuji cetak fisik pada 100% dan diuji baca sebelum digunakan untuk ujian sungguhan.
- QR berisi `PKLJK1|NIS|KODE_UJIAN|PAKET`; **tidak mengandung kunci jawaban**. Jangan masukkan informasi sensitif dalam kode ujian.
- Desain LJK Umum menempatkan bulatan NIS dalam grid sempit. Harus divalidasi saat pencetakan dan dapat direvisi jika tingkat pembacaan rendah.
- Build mengandalkan Maven/Google repositories untuk mengunduh ZXing QR dependency.

## Rencana selanjutnya
- v1.1: CameraX, deteksi marker, koreksi perspektif, pembaca OMR dari foto.
- v1.2: Live Scan tanpa tombol foto, validasi multi-frame, koreksi otomatis.
- v1.3+: Room untuk database siswa, nilai, dan analisis.
