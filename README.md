# PakKom LJK Scanner v1.6.2 — Fast Live Scanner

Berbasis source v1.6.1 yang berhasil dibuild dan diuji pengguna.

## Perubahan
- Tombol **SCAN BERIKUTNYA (LIVE)** pada halaman hasil untuk langsung kembali ke kamera.
- AUTO memerlukan **3 pembacaan berturut-turut yang identik** sebelum mengambil foto (sebelumnya 2).
- Status stabilitas `n/3` ditampilkan saat AUTO aktif.
- Pencegahan pemicu pengambilan foto berulang dalam jeda 3,5 detik, dan penguncian saat foto diproses.
- AUTO tidak mengambil foto jika kode peserta tidak jelas, ada jawaban `?`, ada peringatan kualitas, atau tidak ada satu pun jawaban yang terisi. Tombol FOTO tetap tersedia.
- Mesin OMR, format koordinat, dan deteksi perspektif tidak diubah.

## Catatan
AUTO masih eksperimental. Pemeriksaan dilakukan sekitar setiap 1,7 detik; 3 pembacaan membutuhkan beberapa detik, bukan pemindaian instan. Status stabil hanya mengacu pada hasil OMR konsisten, belum mengukur pergerakan fisik kamera. Pencegahan duplikasi hanya selama satu sesi kamera, bukan pemeriksaan identitas lembar lintas sesi.

## Instalasi
Ekstrak ZIP ke akar repository GitHub, commit, tunggu workflow Build Android APK, instal APK hasil artifact.

## Uji
1. Live Scanner → FOTO, pastikan hasil akurat.
2. Aktifkan AUTO dan tahan kertas; amati indikator stabil 1/3, 2/3, 3/3.
3. Gunakan SCAN BERIKUTNYA (LIVE) untuk lembar selanjutnya.
4. Pastikan galeri, kamera bawaan, dan format LJK tetap berfungsi.

**Build APK dan pengujian di perangkat belum dilakukan pada paket ini.**
