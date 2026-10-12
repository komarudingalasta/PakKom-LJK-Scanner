# PakKom LJK Scanner v1.3.3 — Hotfix Deteksi Sudut

Fokus: memperbaiki kegagalan mendeteksi empat kotak hitam saat memindai foto dari galeri.

## Perubahan
- Deteksi komponen hitam pada seluruh foto, bukan hanya 25% bagian sudut foto.
- Beberapa ambang hitam (75, 105, 135, 165).
- Seleksi bentuk kotak, kepadatan isi, ukuran, dan konsistensi geometri empat sudut.
- Mengikuti urutan koordinat PDF: kiri atas, kiri bawah, kanan atas, kanan bawah.
- Algoritma pembacaan bulatan v1.3.1 dan branding v1.3.2 tetap dipertahankan.

## Cara pasang
Ekstrak ZIP lalu upload seluruh isi folder ke root repository GitHub. Commit dan jalankan Build Android APK pada Actions.

## Batasan
Perubahan ini belum diuji dengan foto asli pada Android dan belum diverifikasi build APK-nya. Jika foto masih gagal, perlu contoh foto asli dan log diagnostik. Jangan gunakan hasil OMR untuk penilaian resmi sebelum uji akurasi.
