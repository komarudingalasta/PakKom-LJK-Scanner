# PakKom LJK Scanner v1.6.5 — Compact Retro Modern

Perbaikan UI berdasarkan screenshot v1.6.4: judul, kartu format, area scan, dan tombol dibuat lebih ringkas dan proporsional. Warna toska, oranye, dan gading dipertahankan.

## Perubahan
- Header diperkecil dan disederhanakan.
- Kartu format lebih padat; area scan tidak lagi mendominasi layar.
- Tombol utama **Mulai Scan** dan tombol sekunder **Pilih dari Galeri**.
- Status ringkas di bawah kartu.
- Halaman hasil menggunakan tipografi lebih seimbang.
- Versi aplikasi 1.6.5.

## Tidak diubah
`OmrReader.kt`, `LiveScannerActivity.kt`, aset JSON koordinat, algoritma deteksi, dan alur pemindaian.

## Instalasi
Ekstrak seluruh isi ZIP ke akar repository GitHub `PakKom-LJK-Scanner` dan commit. Tunggu GitHub Actions, lalu unduh APK dari Artifacts bila build berhasil.

**Catatan:** Ini implementasi native Android yang mengikuti komposisi opsi 3 secara lebih ketat; bukan salinan piksel-per-piksel dari gambar konsep. Belum diuji pada emulator/perangkat dan build APK belum dijalankan di lingkungan ini.


## v1.6.6 — UI Rebuild
Beranda dirombak: header berikon, format satu baris, kartu scan horizontal, galeri sebagai aksi sekunder. Mesin OMR, CameraX, format JSON, dan logika hasil tidak diubah. APK belum diuji build/perangkat.
