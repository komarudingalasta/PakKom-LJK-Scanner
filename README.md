# PakKom LJK Scanner v1.5 — Stability

Versi konservatif dari v1.4 yang dilaporkan berhasil pada uji kertas. **Mesin OMR dan validasi perspektif v1.4 tidak diubah.**

Perubahan: label versi, petunjuk uji kemiringan 0°/10°/20°, tombol bersihkan hasil/scan ulang, dan status bahwa perspektif tervalidasi jika pembacaan berhasil. Tidak ada database, nilai, analisis, atau penyimpanan.

## Uji penerimaan
1. Cetak PDF A5 20 soal A–D pada skala 100%.
2. Isi kode peserta dan jawaban yang diketahui; foto lurus 0°, lalu 10° dan 20° dengan semua penanda terlihat.
3. Cocokkan kode, setiap jawaban, dan jawaban kosong. Bandingkan kamera dan galeri.
4. Jika salah satu foto gagal, jangan gunakan hasilnya untuk penilaian. Kirim foto dan tampilan overlay untuk kalibrasi.

## GitHub
Ekstrak ZIP, unggah **isi** folder ke root repository (termasuk `.github`), commit, jalankan workflow Build Android APK. Build dan akurasi belum diuji di perangkat dalam lingkungan ini.
