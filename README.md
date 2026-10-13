# PakKom LJK Scanner v1.8.0 — Minimalist LJK Generator

Berbasis source v1.7.0. Beranda memisahkan tampilan ukuran kertas, jumlah soal, dan pilihan jawaban. Menu **Buat LJK PDF** menyediakan pengaturan terpisah, judul ujian, dan nama sekolah opsional.

**PENTING:** Untuk menjaga kesesuaian dengan OMR yang sudah teruji, generator hanya mengizinkan tiga kombinasi yang memiliki koordinat JSON yang sama persis dengan scanner: A5/20/A–D, A5/30/A–E, A4/50/A–E. Kombinasi lain ditolak, bukan menghasilkan LJK yang tak dapat dibaca.

PDF dihasilkan offline melalui Android PdfDocument dan disimpan dengan pemilih dokumen Android (tanpa izin penyimpanan). Penanda sudut dan semua posisi gelembung diambil langsung dari `scan-*.json`, sama seperti `OmrReader`. Layout dibuat minimalis, hitam-putih, dengan judul, nama, kelas, kode peserta, dan soal.

**Cetak 100% / Actual Size**; jangan gunakan Fit to Page. Lakukan uji cetak fisik, foto, dan verifikasi seluruh nomor sebelum dipakai ujian.

OMR dan CameraX tidak diubah. Build APK serta uji fisik belum dijalankan di lingkungan ini.
