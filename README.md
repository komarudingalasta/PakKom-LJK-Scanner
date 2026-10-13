# PakKom LJK Scanner v1.6.4 — Retro Modern (Opsi 3)

Implementasi native Android Kotlin untuk **Opsi 3: Retro Modern** (hijau toska, oranye hangat, putih gading). Dibuat dari source v1.6.3 dengan desain beranda dan halaman hasil yang lebih ringkas.

## Perubahan
- Beranda: header sederhana, satu pemilih format, satu tombol utama **Mulai Scan**, satu tombol **Pilih dari Galeri**. Kamera bawaan dan bantuan tersedia lewat ikon pengaturan.
- Hasil: kode peserta dan status di bagian atas, tabel jawaban dua kolom, foto/overlay, lalu tombol **Scan Berikutnya**.
- Live Scanner: tetap memakai CameraX dan kontrol AUTO/FOTO sebelumnya, warna menyesuaikan tema.
- Memperbaiki kesalahan kompilasi sebelumnya: `isFillViewport = true`.
- **OmrReader.kt, ScanGuideView.kt, JSON koordinat, dan logika capture tidak diubah.**

## Memasang
Ekstrak ZIP ke root repository GitHub (bukan sebagai satu folder tambahan), commit, lalu periksa Actions > Artifacts. Sebelum unggah, simpan versi terakhir yang berfungsi sebagai cadangan.

## Status validasi
Pemeriksaan statis file dan ZIP dilakukan. **Build Android/instalasi pada perangkat belum diuji**; periksa GitHub Actions. Ini adalah implementasi tema Opsi 3 yang mendekati arah desain yang dipilih, bukan jaminan identik piksel demi piksel dengan gambar konsep.
