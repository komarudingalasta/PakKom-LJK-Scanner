# PakKom LJK Scanner v1.6.3 — Retro Edition

Android Kotlin / CameraX, offline. Source lengkap untuk repository GitHub.

## Yang berubah
- Beranda retro krem–hijau botol–cokelat, pilihan format, satu tombol scan utama, dan galeri.
- Kamera bawaan dipindahkan ke menu **Kamera bawaan • Bantuan**.
- Hasil scan menjadi layar terpisah: foto overlay, kode peserta, jawaban dalam dua kolom, dan tombol **Scan Berikutnya**.
- Live Camera diberi header dan panel kontrol bernuansa retro.
- Tidak menambah database, penyimpanan hasil, maupun penilaian.
- **OmrReader.kt, ScanGuideView.kt, seluruh JSON koordinat, dan logika auto capture tidak diubah.**

## Cara build
Ekstrak isi ZIP ke root repository `PakKom-LJK-Scanner`, commit, lalu jalankan GitHub Actions `Build Android APK` dan unduh artifact APK.

## Pengujian wajib
1. Pastikan GitHub Actions berhasil build.
2. Pilih A5 20 dan pindai dari Live Scanner; cek kode peserta dan jawaban.
3. Uji galeri, kamera bawaan dari menu bantuan, dan Scan Berikutnya.
4. Uji pemilihan A5 30 / A4 50 (format harus tetap benar setelah pindah halaman).
5. Cocokkan seluruh overlay dengan kertas asli.

**Catatan:** source diperiksa secara statis; build APK dan pengujian perangkat belum dilakukan di sini. Desain retro ini adalah implementasi UI native yang disederhanakan dari konsep visual, bukan salinan piksel demi piksel.
