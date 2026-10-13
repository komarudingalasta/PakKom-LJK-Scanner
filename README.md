# PakKom LJK Scanner v1.6.7 — Balanced Scanner UI

Berbasis source v1.6.6. Perubahan hanya pada **beranda** dan versi aplikasi.

- Ikon scanner, kamera, galeri, pengaturan, dan keamanan digambar dengan Canvas Android (`ScannerIconView.kt`), tidak memakai karakter Unicode atau emoji.
- Kartu panduan LJK baru (`LjkGuideView.kt`) memperlihatkan empat penanda sudut, tanpa menyentuh mesin OMR.
- Kartu format ringkas, kartu aksi seimbang, tombol galeri di dalam kartu, footer terdorong ke bawah jika layar tinggi.
- `OmrReader.kt`, `LiveScannerActivity.kt`, file koordinat JSON, dan alur kamera tetap sama dengan v1.6.6.

## Pemasangan
Ekstrak ZIP dan unggah seluruh isi folder ke root repository GitHub, bukan folder pembungkusnya. Commit, tunggu GitHub Actions hijau, unduh APK dari Artifacts. Pastikan header menampilkan `v1.6.7`.

## Verifikasi
Periksa beranda di HP kecil dan besar, pergantian format, tombol Scan/Galeri/Pengaturan, dan satu foto LJK cetak yang sebelumnya terbaca benar. Build Android dan uji perangkat belum dijalankan di lingkungan pembuatan source.
