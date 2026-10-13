# PakKom LJK Scanner v1.6 — Live Camera (eksperimental)

Berbasis source v1.5.4. Menambahkan kamera CameraX langsung dalam aplikasi, panduan posisi kertas, tombol foto manual, dan opsi auto capture **mati secara default**. Kamera bawaan dan galeri tetap tersedia. Mesin `OmrReader.kt` tidak diubah.

## Cara pakai
1. Buka **BUKA LIVE SCANNER**, izinkan kamera.
2. Pastikan keempat kotak hitam LJK masuk bingkai dan kertas cukup terang.
3. Tekan **FOTO** untuk pengambilan manual, atau hidupkan **AUTO** untuk percobaan pengambilan otomatis setelah dua analisis berturut-turut konsisten.
4. Hasil dibaca dari foto resolusi penuh oleh OMR yang sama seperti sebelumnya.

**Penting:** Auto capture eksperimental dan dapat lambat di ponsel tertentu karena analisis frame cukup berat. Indikator bingkai hanyalah panduan, bukan indikator deteksi sudut. Deteksi empat sudut tetap dilakukan oleh OmrReader. Tidak ada database, nilai, atau penyimpanan hasil.

## Pengujian
Jalankan `python3 tools/check_layout.py && python3 tools/check_scan_layouts.py`, kemudian build GitHub Actions. Uji pembacaan kode peserta dan jawaban pada kertas asli, terutama nomor kosong. Jangan menganggap hasil build atau auto capture sebagai bukti akurasi OMR.
