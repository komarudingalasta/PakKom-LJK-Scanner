# PakKom LJK Scanner v1.5.2 — Quality Check

Pembaruan konservatif dari v1.5.1. **Algoritma deteksi penanda, perspektif, dan penilaian bulatan tidak diubah.**

## Yang baru
- Pemeriksaan indikatif pencahayaan setelah gambar diluruskan.
- Peringatan jika terdapat area gelap atau kontras terang-gelap yang besar (kemungkinan bayangan).
- Informasi kualitas foto tampil bersama hasil scan; peringatan **tidak** mengubah jawaban atau menggagalkan scan.
- Versi aplikasi menjadi 1.5.2 (versionCode 152).

## Batasan
Pemeriksaan kualitas hanya heuristik; tidak membuktikan foto tajam atau hasil OMR akurat. Belum ada pemindaian kamera langsung otomatis. APK belum dibangun atau diuji pada perangkat di lingkungan ini.

## Instal
Ekstrak isi ZIP ke root repository GitHub, commit, jalankan workflow Actions, unduh APK dari Artifacts. Uji kode 213 dan jawaban di kertas yang sama pada cahaya merata dan bayangan.
