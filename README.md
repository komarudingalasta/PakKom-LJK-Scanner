# PakKom LJK Scanner Android v1.1 - Universal A4/A5

**Source Android Studio untuk upload ke root repository GitHub**. Ini adalah aplikasi generator PDF, **belum scanner kamera/OMR**.

## Fitur
- LJK Universal A4 dan A5, 50 soal A-E
- Kode peserta 3 digit, pilihan paket A-D
- Nama/kelas/tanggal ditulis siswa
- PDF vektor dengan koordinat OMR dalam assets/coordinates-A4.json dan coordinates-A5.json
- Simpan PDF via Android Storage Access Framework
- GitHub Actions membangun APK debug dan mengunggah artifact

## Cara upload
Ekstrak ZIP, lalu unggah **isi folder** ke root repository (termasuk `.github/workflows/build-apk.yml`). Hapus file lama `app/src/main/assets/omr_coordinates_v1.json` jika masih ada. Hapus workflow lama yang tidak diperlukan. Jalankan Actions > Build Android APK.

## Catatan
- APK belum berhasil diverifikasi melalui GitHub Actions dari lingkungan ini.
- PDF dan koordinat diadaptasi dari generator v1.1. Akurasi pemindaian kamera belum diuji.
- Kode peserta 000-999 hanya unik di lingkup ujian aktif.
- Cetak PDF pada 100% (actual size), jangan fit-to-page.
