# PakKom LJK Scanner v1.4.0 — Perspective Safety

Hotfix untuk mencegah hasil OMR palsu akibat deteksi empat penanda yang salah.

- Memvalidasi rasio fisik antarpenanda sesuai format PDF.
- Memeriksa konsistensi ukuran penanda, bentuk, area dan distorsi perspektif.
- Memeriksa ulang keempat penanda setelah gambar diluruskan.
- Jika validasi gagal, proses pembacaan **dihentikan** (tidak mengeluarkan jawaban palsu).
- Tidak menambah penyimpanan atau analisis.

**Penting:** Versi ini mengutamakan keselamatan pembacaan; beberapa foto yang kurang baik mungkin ditolak. Belum ada uji akurasi dengan foto asli dan belum diverifikasi build Android. Gunakan foto seluruh kertas yang lurus, tidak terpotong, dengan empat kotak hitam terlihat.

Unggah seluruh isi folder ini ke root repository GitHub, kemudian jalankan workflow Build Android APK.
