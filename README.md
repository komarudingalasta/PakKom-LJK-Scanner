# PakKom LJK Scanner v1.5.3 — False Positive Fix

Basis: source v1.5.2. Perubahan terfokus pada `OmrReader.kt`.

## Perubahan
- Batas minimum skor isi diperketat: jawaban 0,34; digit 0,31.
- Selisih skor pilihan tertinggi dan kedua minimal 0,20; pilihan kedua harus kurang dari 62% skor tertinggi.
- Jawaban samar ditandai `?` (bukan dipaksakan sebagai pilihan).
- Peringatan untuk soal ambigu kini menyertakan skor A/B/C/D agar penyebab dapat ditelusuri.
- Algoritma registrasi dan koreksi perspektif tidak diubah.

## Penting
Ini merupakan *hotfix konservatif*, bukan jaminan pembacaan akurat. Ambang ini dapat meningkatkan jumlah `?` pada arsiran tipis. Jangan gunakan untuk penilaian nyata sebelum menguji sejumlah lembar dengan kunci hasil manual. Jika nomor 4/6 yang kosong masih terbaca A, kirim skor diagnostik pada bagian PERIKSA MANUAL agar dapat dilakukan kalibrasi berdasarkan data.

## Instalasi
Ekstrak ZIP di akar repository GitHub dan commit. Build melalui GitHub Actions.
