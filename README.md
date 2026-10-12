# PakKom LJK Scanner v1.3 — Scanner Core

Fokus: kamera, galeri, pembacaan kode 3 digit dan bulatan, serta overlay inspeksi. **Belum** ada penilaian atau penyimpanan.

## Perbaikan
- Hapus izin CAMERA dari manifest karena aplikasi menggunakan intent kamera eksternal; menghindari Permission Denial yang dilaporkan pada Samsung.
- Tambahkan izin URI sementara untuk aplikasi kamera.
- Baca orientasi EXIF foto galeri/kamera.
- Gunakan skor kontras relatif pusat bulatan vs lingkungan (masih perlu kalibrasi pada hasil cetak nyata).
- Tampilkan foto hasil koreksi perspektif dengan lingkaran hijau untuk pilihan terdeteksi dan oranye untuk pilihan lain.
- Tandai hasil yang ambigu, jangan gunakan sebagai nilai resmi.

## Cara unggah
Ekstrak ZIP, upload **isi folder** ke root repository GitHub (termasuk `.github`), commit satu kali, buka Actions > Build Android APK > Artifacts.

## Uji wajib
Cetak PDF asli pada skala 100%, isi kode dan jawaban, foto 4 penanda sudut, lalu bandingkan hasil dengan overlay. Jika hasil salah, kirim foto asli dan screenshot overlay. Build sukses **tidak** berarti akurasi OMR sudah teruji.

## Catatan
Format scan masih dibatasi ke tiga file JSON: A5 20 A-D, A5 30 A-E, A4 50 A-E. Tidak otomatis mendukung seluruh custom generator. Kinerja belum diverifikasi dengan Android SDK atau perangkat fisik dalam lingkungan pembuatan ini.
